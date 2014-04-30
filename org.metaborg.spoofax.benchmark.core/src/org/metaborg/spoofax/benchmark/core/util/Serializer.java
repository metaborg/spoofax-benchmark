package org.metaborg.spoofax.benchmark.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class Serializer<T> {
	public final Class<T> type;


	public Serializer(Class<T> type) {
		this.type = type;
	}


	public void serialize(T data, File filename) throws IOException {
		final OutputStream stream = new FileOutputStream(filename);
		final FSTObjectOutput out = new FSTObjectOutput(stream);
		try {
			out.writeObject(data, type);
			out.flush();
		} finally {
			out.close();
		}
	}

	@SuppressWarnings("unchecked")
	public T deserialize(File filename) throws Exception {
		final InputStream stream = new FileInputStream(filename);
		final FSTObjectInput in = new FSTObjectInput(stream);
		try {
			return (T) in.readObject(type);
		} finally {
			in.close();
		}
	}
}
