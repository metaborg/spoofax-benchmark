package org.metaborg.spoofax.benchmark.core.export.history;

import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;

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

public class ImageHistoryExporter {
	public void export(Iterable<ProcessedData> historicalData, File directory) throws IOException {
		writeTimePlot(historicalData, new File(directory, "time.png"));
	}

	private void writeTimePlot(Iterable<ProcessedData> historicalData, File filename) throws IOException {
		final XYSeriesCollection dataset = createTimeDataset(historicalData);

		final JFreeChart chart =
			ChartFactory.createXYLineChart("Speed of analysis over time", "Datapoint", "Time in seconds", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		final XYPlot plot = (XYPlot) chart.getPlot();

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for(int i = 0; i < dataset.getSeriesCount(); ++i) {
			renderer.setSeriesShapesVisible(i, true);
			renderer.setSeriesStroke(i, new BasicStroke(2));
		}
		plot.setRenderer(renderer);

		final NumberAxis valueAxis = (NumberAxis) plot.getDomainAxis();
		valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		ChartUtilities.saveChartAsPNG(filename, chart, 800, 600);
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
			parseTimeSeries.add(i, data.time.parse);
			collectTimeSeries.add(i, data.time.collect);
			taskEvalTimeSeries.add(i, data.time.taskEval);
			indexPersistTimeSeries.add(i, data.time.indexPersist);
			taskPersistTimeSeries.add(i, data.time.taskPersist);
			totalTimeSeries.add(i, data.time.total());
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
}
