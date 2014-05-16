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

import com.beust.jcommander.internal.Maps;

public class ImageHistoryExporter {
	public void export(Iterable<ProcessedData> historicalData, File directory) throws IOException {
		writeXYPlot(createTimeDataset(historicalData), new File(directory, "time.png"), "Speed of analysis over time",
			"Datapoint", "Time in seconds");

		// size over time


	}

	private XYSeriesCollection createTimeDataset(Iterable<ProcessedData> historicalData) {
		final XYSeries parseTimeSeries = new XYSeries("Parse");
		final XYSeries collectTimeSeries = new XYSeries("Collect");
		final XYSeries taskEvalTimeSeries = new XYSeries("Task eval");
		final XYSeries indexPersistTimeSeries = new XYSeries("Index persist");
		final XYSeries taskPersistTimeSeries = new XYSeries("Task engine persist");
		final XYSeries totalTimeSeries = new XYSeries("Total");

		int i = 1;
		for(ProcessedData data : historicalData) {
			parseTimeSeries.add(i, data.time.parse.mean());
			collectTimeSeries.add(i, data.time.collect.mean());
			taskEvalTimeSeries.add(i, data.time.taskEval.mean());
			indexPersistTimeSeries.add(i, data.time.indexPersist.mean());
			taskPersistTimeSeries.add(i, data.time.taskPersist.mean());
			totalTimeSeries.add(i, data.time.totalByMean());
			++i;
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(parseTimeSeries);
		dataset.addSeries(collectTimeSeries);
		dataset.addSeries(taskEvalTimeSeries);
		dataset.addSeries(indexPersistTimeSeries);
		dataset.addSeries(taskPersistTimeSeries);
		dataset.addSeries(totalTimeSeries);

		return dataset;
	}

	private void createMapsDataset(Iterable<Map<String, Number>> maps) {
		final Map<String, XYSeries> allSeries = Maps.newHashMap();
		int i = 1;
		for(final Map<String, Number> map : maps) {
			for(final Entry<String, Number> entry : map.entrySet()) {
				XYSeries series = allSeries.get(entry.getKey());
				if(series == null) {
					series = new XYSeries(entry.getKey());
					allSeries.put(entry.getKey(), series);
				}

				series.add(i, entry.getValue());
			}
			++i;
		}
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
