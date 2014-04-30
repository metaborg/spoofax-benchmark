package org.metaborg.spoofax.benchmark.core.export.single;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public final class CSVSingleExporter {
	public void export(ProcessedData data, File directory) throws Exception {
		FileUtils.forceMkdir(directory);
		
		writePrims(data.time, new File(directory, "time.csv"));
		writePrims(data.index, new File(directory, "index.csv"));
		writeMultiset(data.index.numKinds, new File(directory, "index-kinds.csv"));
		writePrims(data.taskEngine, new File(directory, "taskengine.csv"));
		writeMultiset(data.taskEngine.numKinds, new File(directory, "taskengine-kinds.csv"));
	}

	private void writePrims(Object object, File file) throws IllegalArgumentException, IllegalAccessException,
		IOException {
		final Map<String, Object> values = Maps.newLinkedHashMap();
		for(Field field : object.getClass().getFields()) {
			final Class<?> type = field.getType();
			if(type.equals(long.class))
				values.put(field.getName(), field.getLong(object));
			else if(type.equals(double.class))
				values.put(field.getName(), field.getDouble(object));
		}
		write(values, file);
	}
	
	private void writeMultiset(Multiset<String> multiset, File file) throws IOException {
		final Map<String, Object> values = Maps.newLinkedHashMap();
		for(Entry<String> entry : multiset.entrySet()) {
			values.put(entry.getElement(), entry.getCount());
		}
		write(values, file);
	}

	private void write(Map<String, Object> values, File file) throws IOException {
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
			for(Object value : values.values()) {
				if(!first)
					builder.append(", ");
				builder.append(value.toString());
				first = false;
			}
			writer.println(builder.toString());

			writer.flush();
		} finally {
			writer.close();
		}
	}
}
