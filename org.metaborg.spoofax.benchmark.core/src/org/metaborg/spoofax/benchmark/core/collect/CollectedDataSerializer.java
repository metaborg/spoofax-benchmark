package org.metaborg.spoofax.benchmark.core.collect;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
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


    public void serialize(CollectedData data, FileObject directory) throws IOException {
        directory.createFolder();

        IndexManager.write(data.index, IndexManager.cacheFile(directory), termFactory);
        TaskManager.write(data.taskEngine, TaskManager.cacheFile(directory), termFactory);
        serializer.serialize(data, rawData(directory));
    }

    public CollectedData deserialize(FileObject directory) throws Exception {
        final CollectedData data = serializer.deserialize(rawData(directory));
        final FileObject indexFile = IndexManager.cacheFile(directory);
        data.indexFile = indexFile;
        data.index = IndexManager.read(indexFile, termFactory);
        final FileObject taskEngineFile = TaskManager.cacheFile(directory);
        data.taskEngineFile = taskEngineFile;
        data.taskEngine = TaskManager.read(taskEngineFile, termFactory);
        return data;
    }

    
    private static FileObject rawData(FileObject directory) throws FileSystemException {
        return directory.resolveFile("rawdata.dat");
    }
}
