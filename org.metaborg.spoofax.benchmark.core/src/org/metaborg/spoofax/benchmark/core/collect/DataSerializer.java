package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.runtime.task.engine.TaskManager;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.ITermFactory;

public final class DataSerializer {
	private final ITermFactory termFactory;
	private final IOAgent agent;

	public DataSerializer(ITermFactory termFactory, IOAgent agent) {
		this.termFactory = termFactory;
		this.agent = agent;
	}

	public void serialize(RawData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		IndexManager.getInstance().write(data.index, new File(directory, "index.idx"), termFactory);
		TaskManager.getInstance().write(data.taskEngine, new File(directory, "taskengine.idx"), termFactory);

		// TODO: serialize time
	}

	public RawData deserialize(File directory) {
		final IIndex index =
			IndexManager.getInstance().read(new File(directory, "index.idx"), termFactory, agent);
		final ITaskEngine taskEngine =
			TaskManager.getInstance().read(new File(directory, "index.idx"), termFactory);

		// TODO: deserialize time

		final RawData data = new RawData();
		data.index = index;
		data.taskEngine = taskEngine;
		return data;
	}
}
