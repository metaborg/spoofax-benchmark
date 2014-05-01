package org.metaborg.spoofax.benchmark.core.export.single;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.SortOrder;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;
import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class ImageSingleExporter {
	public void export(ProcessedData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		exportIndex(data, directory);
		exportTaskEngine(data, directory);
		exportTime(data, directory);
	}

	private void exportIndex(ProcessedData data, File directory) throws IOException {
		writeMultisetPie(data.index.kinds, new File(directory, "index-kinds.png"), "Index entry kinds", "0", "0%");

		writeMathMeanPie(data.index.uriLengthPerKind, new File(directory, "index-uri-segment-length.png"),
			"Index URI segment length (mean) distribution per entry kind", "0.000", "0%");

		writeMultisetPie(data.index.uriSegmentKinds, new File(directory, "index-uri-segment-kinds.png"),
			"Index URI segment kinds", "0", "0%");

		final Map<String, Object> indexSizeMap = Maps.newLinkedHashMap();
		indexSizeMap.put("Persisted", data.index.diskSize);
		indexSizeMap.put("Memory", data.index.memSize);
		writeMapPie(indexSizeMap, new File(directory, "index-size.png"), "Absolute index size", "0.000MB", "0%");
	}

	private void exportTaskEngine(ProcessedData data, File directory) throws IOException {
		writeMultisetPie(data.taskEngine.instructionKinds, new File(directory, "taskengine-kinds.png"),
			"Task engine, instruction kinds", "0", "0%");

		writeMathSumPie(data.taskEngine.evaluationsPerKind, new File(directory, "taskengine-evaluations-sum.png"),
			"Task engine, number of evaluations (sum) per instruction kind", "0", "0%");
		writeMathMeanPie(data.taskEngine.evaluationsPerKind, new File(directory, "taskengine-evaluations-mean.png"),
			"Task engine, number of evaluations (mean) per instruction kind", "0.000", "0%");

		writeMathSumPie(data.taskEngine.evaluationTimesPerKind, new File(directory, "taskengine-evaluation-times-sum.png"),
			"Task engine, single evaluation times (sum) per instruction kind", "0", "0%");
		writeMathMeanPie(data.taskEngine.evaluationTimesPerKind, new File(directory, "taskengine-evaluation-times-mean.png"),
			"Task engine, single evaluation times (mean) per instruction kind", "0.000", "0%");
		
		writeMultisetPie(data.taskEngine.taskTypes, new File(directory, "taskengine-types.png"),
			"Task engine, task types", "0", "0%");
		
		// TODO: dependencies
		writeMathSumPie(data.taskEngine.dependencyTrailLengthPerKind, new File(directory, "taskengine-dep-trail-sum.png"),
			"Task engine, length of dependency trail (sum) for root tasks per instruction kind", "0", "0%");
		writeMathMeanPie(data.taskEngine.dependencyTrailLengthPerKind, new File(directory, "taskengine-dep-trail-mean.png"),
			"Task engine, length of dependency trail (mean) for root tasks per instruction kind", "0.000", "0%");
		
		writeMultisetPie(data.taskEngine.taskStatusKinds, new File(directory, "taskengine-status-kinds.png"),
			"Task engine, task status kinds", "0", "0%");
		
		writeMathSumPie(data.taskEngine.numResultsPerKind, new File(directory, "taskengine-results-sum.png"),
			"Task engine, number of results (sum) per instruction kind", "0", "0%");
		writeMathMeanPie(data.taskEngine.numResultsPerKind, new File(directory, "taskengine-results-mean.png"),
			"Task engine, number of results (mean) per instruction kind", "0.000", "0%");

		final Map<String, Object> taskEngineSizeMap = Maps.newLinkedHashMap();
		taskEngineSizeMap.put("Persisted", data.taskEngine.diskSize);
		taskEngineSizeMap.put("Memory", data.taskEngine.memSize);
		writeMapPie(taskEngineSizeMap, new File(directory, "taskengine-size.png"), "Absolute task engine size",
			"0.000MB", "0%");
	}

	private void exportTime(ProcessedData data, File directory) throws IOException {
		final Map<String, Object> timeMap = Maps.newLinkedHashMap();
		timeMap.put("Parse", data.time.parse);
		timeMap.put("Collect", data.time.collect);
		timeMap.put("Perform", data.time.taskEval);
		timeMap.put("Persist index", data.time.indexPersist);
		timeMap.put("Persist task engine", data.time.taskPersist);
		writeMapPie(timeMap, new File(directory, "time.png"), "Absolute time taken for each phase", "0.000s", "0%");
	}


	private void
		writeMultisetPie(Multiset<String> multiset, File file, String title, String absFormat, String perFormat)
			throws IOException {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(Entry<String> entry : multiset.entrySet()) {
			dataset.setValue(entry.getElement(), (Number) entry.getCount());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		writePie(dataset, file, title, absFormat, perFormat);
	}

	private void writeMapPie(Map<String, Object> map, File file, String title, String absFormat, String perFormat)
		throws IOException {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, Object> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		writePie(dataset, file, title, absFormat, perFormat);
	}

	private void writeMathMeanPie(Map<String, MathLongList> map, File file, String title, String absFormat,
		String perFormat) throws IOException {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, MathLongList> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue().mean());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		writePie(dataset, file, title, absFormat, perFormat);
	}
	
	private void writeMathSumPie(Map<String, MathLongList> map, File file, String title, String absFormat,
		String perFormat) throws IOException {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, MathLongList> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue().sum());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		writePie(dataset, file, title, absFormat, perFormat);
	}


	private void writePie(DefaultPieDataset dataset, File file, String title, String absFormat, String perFormat)
		throws IOException {
		final JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})", new DecimalFormat(absFormat),
			new DecimalFormat(perFormat)));
		ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
		// TODO: SVG output: http://dolf.trieschnigg.nl/jfreechart/
	}
}
