package org.metaborg.spoofax.benchmark.core.process;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.benchmark.core.util.Serializer;

public class ProcessedDataSerializer {
	private final Serializer<ProcessedData> serializer = new Serializer<ProcessedData>(ProcessedData.class);

	public void serialize(ProcessedData data, File filename) throws IOException {
		FileUtils.forceMkdir(filename.getParentFile());
		filename.createNewFile();
		serializer.serialize(data, filename);
	}

	public ProcessedData deserialize(File filename) throws Exception {
		return serializer.deserialize(filename);
	}
}
