package org.metaborg.spoofax.benchmark.core.process;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.benchmark.core.util.KryoSerializer;

public class ProcessedDataSerializer {
    private final KryoSerializer<ProcessedData> serializer;


    public ProcessedDataSerializer() {
        this.serializer = new KryoSerializer<ProcessedData>(ProcessedData.class);
    }


    public void serialize(ProcessedData data, FileObject file) throws IOException {
        file.createFile();
        serializer.serialize(data, file);
    }

    public ProcessedData deserialize(FileObject file) throws Exception {
        return serializer.deserialize(file);
    }
}
