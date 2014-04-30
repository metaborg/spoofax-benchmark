package org.metaborg.spoofax.benchmark.core.export.history;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

public class ImageHistoryExporter {
	public void export(Iterable<ProcessedData> historicalData, File directory) throws IOException {
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

		final JFreeChart chart =
			ChartFactory.createXYLineChart("Speed of analysis over time", "Datapoint", "Time in seconds", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		ChartUtilities.saveChartAsPNG(new File(directory, "time.png"), chart, 800, 600);
	}
}
