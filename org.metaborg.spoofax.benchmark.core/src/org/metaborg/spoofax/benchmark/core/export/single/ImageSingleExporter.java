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

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class ImageSingleExporter {
	public void export(ProcessedData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		writeMultisetPie(data.index.kinds, new File(directory, "index-kinds-pie.png"), "Index entry kinds", "0", "0%");
		writeMultisetPie(data.taskEngine.instructionKinds, new File(directory, "taskengine-kinds-pie.png"),
			"Task engine instruction kinds", "0", "0%");

		final Map<String, Object> timeMap = Maps.newLinkedHashMap();
		timeMap.put("Parse", data.time.parse);
		timeMap.put("Collect", data.time.collect);
		timeMap.put("Perform", data.time.taskEval);
		timeMap.put("Persist index", data.time.indexPersist);
		timeMap.put("Persist task engine", data.time.taskPersist);
		writeMapPie(timeMap, new File(directory, "time-pie.png"), "Absolute time taken for each phase", "0.000s", "0%");
		
		final Map<String, Object> indexSizeMap = Maps.newLinkedHashMap();
		indexSizeMap.put("Persisted", data.index.diskSize);
		indexSizeMap.put("Memory", data.index.memSize);
		writeMapPie(indexSizeMap, new File(directory, "index-size.png"), "Absolute index size", "0.000MB", "0%");
		
		final Map<String, Object> taskEngineSizeMap = Maps.newLinkedHashMap();
		taskEngineSizeMap.put("Persisted", data.taskEngine.diskSize);
		taskEngineSizeMap.put("Memory", data.taskEngine.memSize);
		writeMapPie(taskEngineSizeMap, new File(directory, "taskengine-size.png"), "Absolute task engine size", "0.000MB", "0%");
	}

	private void
		writeMultisetPie(Multiset<String> multiset, File file, String title, String absFormat, String perFormat)
			throws IOException {
		final Map<String, Object> map = Maps.newLinkedHashMap();
		for(Entry<String> entry : multiset.entrySet()) {
			map.put(entry.getElement(), entry.getCount());
		}
		writeMapPie(map, file, title, absFormat, perFormat);
	}

	private void writeMapPie(Map<String, Object> map, File file, String title, String absFormat, String perFormat)
		throws IOException {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		for(java.util.Map.Entry<String, Object> entry : map.entrySet()) {
			dataset.setValue(entry.getKey(), (Number) entry.getValue());
		}
		dataset.sortByValues(SortOrder.DESCENDING);
		final JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})", new DecimalFormat(absFormat),
			new DecimalFormat(perFormat)));
		ChartUtilities.saveChartAsPNG(file, chart, 800, 600);
		// TODO: SVG output: http://dolf.trieschnigg.nl/jfreechart/
	}
}
