package org.metaborg.spoofax.benchmark.core.export.history;

import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;
import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.beust.jcommander.internal.Maps;

public class ImageHistoryExporter {
    public void export(Iterable<ProcessedData> historicalData, File directory) throws IOException {
        writeXYPlot(createTimeDataset(historicalData), new File(directory, "time.png"), "Speed of analysis over time",
            "Datapoint", "Time in seconds");
    }

    private XYSeriesCollection createTimeDataset(Iterable<ProcessedData> historicalData) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        final Map<String, XYSeries> allSeries = Maps.newHashMap();

        int i = 1;
        for(ProcessedData data : historicalData) {
            for(Entry<String, MathLongList> entry : data.time.map.entrySet()) {
                XYSeries series = allSeries.get(entry.getKey());
                if(series == null) {
                    series = new XYSeries(entry.getKey());
                    dataset.addSeries(series);
                }
                series.add(i, entry.getValue().mean());
            }
            ++i;
        }

        return dataset;
    }

    private void writeXYPlot(XYSeriesCollection dataset, File file, String title, String xName, String yName)
        throws IOException {
        final JFreeChart chart =
            ChartFactory.createXYLineChart(title, xName, yName, dataset, PlotOrientation.VERTICAL, true, true, false);
        final XYPlot plot = (XYPlot) chart.getPlot();

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for(int i = 0; i < dataset.getSeriesCount(); ++i) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesStroke(i, new BasicStroke(2));
        }
        plot.setRenderer(renderer);

        final NumberAxis valueAxis = (NumberAxis) plot.getDomainAxis();
        valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
    }
}
