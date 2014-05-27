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
import org.metaborg.spoofax.benchmark.core.process.TaskDependencyData;
import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class ImageSingleExporter {
	public void export(ProcessedData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		if(data.index != null)
			exportIndex(data, directory);
		if(data.taskEngine != null)
			exportTaskEngine(data, directory);
		if(data.time != null)
			exportTime(data, directory);
	}

	private void exportIndex(ProcessedData data, File directory) throws IOException {
		writePie(createMultisetDataset(data.index.kinds), new File(directory, "index-kinds.png"), "Index entry kinds",
			"0", "0%");

		writePie(createMathSumDataset(data.index.uriLengthPerKind),
			new File(directory, "index-uri-segment-length.png"),
			"Index URI segment length (mean) distribution per entry kind", "0.000", "0%");

		writePie(createMultisetDataset(data.index.uriSegmentKinds), new File(directory, "index-uri-segment-kinds.png"),
			"Index URI segment kinds", "0", "0%");

		final Map<String, Number> indexSizeMap = Maps.newLinkedHashMap();
		indexSizeMap.put("Persisted", data.index.diskSize);
		indexSizeMap.put("Memory", data.index.memSize);
		writePie(createMapDataset(indexSizeMap), new File(directory, "index-size.png"), "Absolute index size",
			"0.000MB", "0%");
	}

	private void exportTaskEngine(ProcessedData data, File directory) throws IOException {
		writePie(createMultisetDataset(data.taskEngine.instructionKinds), new File(directory,
			"taskengine-instruction-kinds.png"), "Task engine, instruction kinds", "0", "0%");

		writePie(createMathSumDataset(data.taskEngine.evaluationsPerKind), new File(directory,
			"taskengine-evaluations-sum.png"), "Task engine, number of evaluations (sum) per instruction kind", "0",
			"0%");
		writePie(createMathMeanDataset(data.taskEngine.evaluationsPerKind), new File(directory,
			"taskengine-evaluations-mean.png"), "Task engine, number of evaluations (mean) per instruction kind",
			"0.000", "0%");

		writePie(createMathSumDataset(data.taskEngine.evaluationTimesPerKind), new File(directory,
			"taskengine-evaluation-times-sum.png"), "Task engine, single evaluation times (sum) per instruction kind",
			"0", "0%");
		writePie(createMathMeanDataset(data.taskEngine.evaluationTimesPerKind), new File(directory,
			"taskengine-evaluation-times-mean.png"),
			"Task engine, single evaluation times (mean) per instruction kind", "0.000", "0%");

		writePie(createMultisetDataset(data.taskEngine.taskKinds), new File(directory, "taskengine-task-kinds.png"),
			"Task engine, task kinds", "0", "0%");

		exportDependencies(data.taskEngine.staticDependenciesPerKind, directory, "taskengine-statdep",
			"Task engine, static");
		exportDependencies(data.taskEngine.dynamicDependenciesPerKind, directory, "taskengine-dyndep",
			"Task engine, dynamic");
		exportDependencies(data.taskEngine.allDependenciesPerKind, directory, "taskengine-alldep", "Task engine, all");

		writePie(createMultisetDataset(data.taskEngine.staticDependencies.status), new File(directory,
			"taskengine-statdep-status.png"), "Task engine, static dependency status", "0", "0%");
		writePie(createMultisetDataset(data.taskEngine.dynamicDependencies.status), new File(directory,
			"taskengine-dyndep-status.png"), "Task engine, dynamic dependency status", "0", "0%");

		writePie(createMultisetDataset(data.taskEngine.allDependencies.status), new File(directory,
			"taskengine-alldep-status.png"), "Task engine, all dependency status", "0", "0%");

		writePie(createMultisetDataset(data.taskEngine.taskKinds), new File(directory, "taskengine-types.png"),
			"Task engine, task types", "0", "0%");

		writePie(createMathSumDataset(data.taskEngine.dependencyTrailLengthPerKind), new File(directory,
			"taskengine-alldep-trail-sum.png"),
			"Task engine, length of dependency trail (sum) for root tasks per instruction kind", "0", "0%");
		writePie(createMathMeanDataset(data.taskEngine.dependencyTrailLengthPerKind), new File(directory,
			"taskengine-alldep-trail-mean.png"),
			"Task engine, length of dependency trail (mean) for root tasks per instruction kind", "0.000", "0%");

		writePie(createMultisetDataset(data.taskEngine.dependencyKind), new File(directory,
			"taskengine-alldep-kinds.png"), "Task engine, dependency kinds", "0", "0%");

		writePie(createMathSumDataset(data.taskEngine.numResultsPerKind), new File(directory,
			"taskengine-results-sum.png"), "Task engine, number of results (sum) per instruction kind", "0", "0%");
		writePie(createMathMeanDataset(data.taskEngine.numResultsPerKind), new File(directory,
			"taskengine-results-mean.png"), "Task engine, number of results (mean) per instruction kind", "0.000", "0%");

		final Map<String, Number> taskEngineSizeMap = Maps.newLinkedHashMap();
		taskEngineSizeMap.put("Persisted", data.taskEngine.diskSize);
		taskEngineSizeMap.put("Memory", data.taskEngine.memSize);
		writePie(createMapDataset(taskEngineSizeMap), new File(directory, "taskengine-size.png"),
			"Absolute task engine size", "0.000MB", "0%");
	}

	private void exportDependencies(Map<String, TaskDependencyData> data, File directory, String filePrefix,
		String titlePrefix) throws IOException {
		final DefaultPieDataset sizeDataset = new DefaultPieDataset();
		final DefaultPieDataset outDataset = new DefaultPieDataset();
		final DefaultPieDataset inDataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, TaskDependencyData> entry : data.entrySet()) {
			sizeDataset.setValue(entry.getKey(), (Number) entry.getValue().size);
			outDataset.setValue(entry.getKey(), (Number) entry.getValue().outDeps);
			inDataset.setValue(entry.getKey(), (Number) entry.getValue().inDeps);
		}
		sizeDataset.sortByValues(SortOrder.DESCENDING);
		outDataset.sortByValues(SortOrder.DESCENDING);
		inDataset.sortByValues(SortOrder.DESCENDING);
		writePie(sizeDataset, new File(directory, filePrefix + "-size.png"), titlePrefix
			+ " dependency size (sum) per instruction kind", "0", "0%");
		writePie(outDataset, new File(directory, filePrefix + "-out.png"), titlePrefix
			+ " outgoing dependencies (sum) per instruction kind", "0", "0%");
		writePie(inDataset, new File(directory, filePrefix + "-in.png"), titlePrefix
			+ " incoming dependencies (sum) per instruction kind", "0", "0%");
	}

	private void exportTime(ProcessedData data, File directory) throws IOException {
		final Map<String, Number> timeMap = Maps.newLinkedHashMap();
		timeMap.put("Parse", data.time.parse.mean());
		timeMap.put("Pre-trans", data.time.preTrans.mean());
		timeMap.put("Collect", data.time.collect.mean());
		timeMap.put("Perform", data.time.taskEval.mean());
		timeMap.put("Post-trans", data.time.postTrans.mean());
		timeMap.put("Persist index", data.time.indexPersist.mean());
		timeMap.put("Persist task engine", data.time.taskPersist.mean());
		writePie(createMapDataset(timeMap), new File(directory, "time.png"), "Absolute time taken for each phase",
			"0.000s", "0%");
	}


	private DefaultPieDataset createMultisetDataset(Multiset<String> multiset) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(Entry<String> entry : multiset.entrySet()) {
			dataset.setValue(entry.getElement(), (Number) entry.getCount());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		return dataset;
	}

	private DefaultPieDataset createMapDataset(Map<String, Number> map) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, Number> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		return dataset;
	}

	private DefaultPieDataset createMathMeanDataset(Map<String, MathLongList> map) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, MathLongList> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue().mean());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		return dataset;
	}

	private DefaultPieDataset createMathSumDataset(Map<String, MathLongList> map) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, MathLongList> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue().sum());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		return dataset;
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
