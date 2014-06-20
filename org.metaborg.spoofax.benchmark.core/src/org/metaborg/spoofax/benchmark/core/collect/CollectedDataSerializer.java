package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.spoofax.benchmark.core.util.KryoSerializer;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.ITermFactory;

public final class CollectedDataSerializer {
	private final ITermFactory termFactory;
	private final KryoSerializer<CollectedData> serializer;

	public CollectedDataSerializer(ITermFactory termFactory) {
		this.termFactory = termFactory;
		this.serializer = new KryoSerializer<CollectedData>(CollectedData.class);
	}

	public void serialize(CollectedData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		IndexManager.getInstance().write(data.index, new File(directory, "index.idx"), termFactory);
		TaskManager.getInstance().write(data.taskEngine, new File(directory, "taskengine.idx"), termFactory);
		serializer.serialize(data, new File(directory, "rawdata.dat"));
	}

	public CollectedData deserialize(File directory) throws Exception {
		final CollectedData data = serializer.deserialize(new File(directory, "rawdata.dat"));
		final File indexFile = new File(directory, "index.idx");
		data.indexFile = indexFile.getAbsolutePath();
		data.index = IndexManager.getInstance().read(indexFile, termFactory);
		final File taskEngineFile = new File(directory, "taskengine.idx");
		data.taskEngineFile = taskEngineFile.getAbsolutePath();
		data.taskEngine = TaskManager.getInstance().read(taskEngineFile, termFactory);
		return data;
	}
}
