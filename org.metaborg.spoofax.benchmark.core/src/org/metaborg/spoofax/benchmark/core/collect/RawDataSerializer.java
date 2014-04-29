package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.metaborg.runtime.task.engine.TaskManager;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.ITermFactory;

import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public final class RawDataSerializer {
	private final ITermFactory termFactory;
	private final IOAgent agent;

	public RawDataSerializer(ITermFactory termFactory, IOAgent agent) {
		this.termFactory = termFactory;
		this.agent = agent;
	}

	public void serialize(RawData data, File directory) throws IOException {
		FileUtils.forceMkdir(directory);

		IndexManager.getInstance().write(data.index, new File(directory, "index.idx"), termFactory);
		TaskManager.getInstance().write(data.taskEngine, new File(directory, "taskengine.idx"), termFactory);

		final OutputStream stream = new FileOutputStream(new File(directory, "rawdata.dat"));
		final FSTObjectOutput out = new FSTObjectOutput(stream);
		try {
			out.writeObject(data, RawData.class);
			out.flush();
		} finally {
			out.close();
		}
	}

	public RawData deserialize(File directory) throws Exception {
		final InputStream stream = new FileInputStream(new File(directory, "rawdata.dat"));
		final FSTObjectInput in = new FSTObjectInput(stream);
		try {
			final RawData data = (RawData) in.readObject(RawData.class);
			data.index = IndexManager.getInstance().read(new File(directory, "index.idx"), termFactory, agent);
			data.taskEngine = TaskManager.getInstance().read(new File(directory, "index.idx"), termFactory);
			return data;
		} finally {
			in.close();
		}
	}
}
