package org.metaborg.spoofax.benchmark.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerializer<T> {
	public final Kryo kryo;
	public final Class<T> type;


	public KryoSerializer(Class<T> type) {
		this.kryo = new Kryo();
		((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
			.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
		this.type = type;
	}


	public void serialize(T data, File filename) throws IOException {
		final OutputStream stream = new FileOutputStream(filename);
		final Output output = new Output(stream);
		try {
			kryo.writeObject(output, data);
			output.flush();
		} finally {
			output.close();
		}
	}

	public T deserialize(File filename) throws Exception {
		final InputStream stream = new FileInputStream(filename);
		final Input input = new Input(stream);
		try {
			return kryo.readObject(input, type);
		} finally {
			input.close();
		}
	}
}
