package org.metaborg.spoofax.benchmark.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
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


    public void serialize(T data, FileObject file) throws IOException {
        file.createFile();
        final OutputStream stream = file.getContent().getOutputStream();
        final Output output = new Output(stream);
        try {
            kryo.writeObject(output, data);
            output.flush();
        } finally {
            output.close();
        }
    }

    public T deserialize(FileObject file) throws Exception {
        final InputStream stream = file.getContent().getInputStream();
        final Input input = new Input(stream);
        try {
            return kryo.readObject(input, type);
        } finally {
            input.close();
        }
    }
}
