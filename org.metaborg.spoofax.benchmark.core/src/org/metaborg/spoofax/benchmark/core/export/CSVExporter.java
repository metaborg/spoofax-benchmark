package org.metaborg.spoofax.benchmark.core.export;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

import com.google.common.collect.Maps;

public final class CSVExporter {
	public void export(ProcessedData data, File directory) throws IOException, IllegalArgumentException,
		IllegalAccessException {
		FileUtils.forceMkdir(directory);
		
		writeCSV(data.time, new File(directory, "time.csv"));
		writeCSV(data.index, new File(directory, "index.csv"));
		writeCSV(data.taskEngine, new File(directory, "taskengine.csv"));
	}

	private void writeCSV(Object object, File file) throws IllegalArgumentException, IllegalAccessException,
		IOException {
		final Map<String, Long> values = Maps.newHashMap();
		for(Field field : object.getClass().getFields()) {
			final Class<?> type = field.getType();
			if(type.equals(long.class))
				values.put(field.getName(), field.getLong(object));
		}
		writeCSV(values, file);
	}

	private void writeCSV(Map<String, Long> values, File file) throws IOException {
		file.createNewFile();
		final PrintWriter writer = new PrintWriter(file);
		try {
			final StringBuilder builder = new StringBuilder();

			boolean first = true;
			for(String name : values.keySet()) {
				if(!first)
					builder.append(", ");
				builder.append(name);
				first = false;
			}
			writer.println(builder.toString());
			builder.setLength(0);

			first = true;
			for(Long value : values.values()) {
				if(!first)
					builder.append(", ");
				builder.append(value);
				first = false;
			}
			writer.println(builder.toString());

			writer.flush();
		} finally {
			writer.close();
		}
	}
}
