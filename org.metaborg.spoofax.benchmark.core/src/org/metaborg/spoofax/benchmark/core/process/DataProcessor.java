package org.metaborg.spoofax.benchmark.core.process;

import java.util.Map;

import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.runtime.task.ITask;
import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.spoofax.benchmark.core.collect.CollectedData;
import org.metaborg.spoofax.benchmark.core.collect.TimeData;
import org.metaborg.spoofax.benchmark.core.util.MathDoubleList;
import org.metaborg.spoofax.benchmark.core.util.MathLongList;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.index.IndexEntry;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class DataProcessor {
    public ProcessedData process(CollectedData rawData, boolean processTimeData, boolean processIndexData,
        boolean processTaskEngineData) throws FileSystemException {
        final TimeData timeData = processTimeData ? processTimeData(rawData) : null;
        final IndexData indexData = processIndexData ? processIndexData(rawData) : null;
        final TaskEngineData taskEngineData = processTaskEngineData ? processTaskEngineData(rawData) : null;

        return new ProcessedData(timeData, indexData, taskEngineData);
    }


    private TimeData processTimeData(CollectedData rawData) {
        return rawData.time;
    }

    private IndexData processIndexData(CollectedData rawData) throws FileSystemException {
        final IndexData data = new IndexData();
        final Iterable<IndexEntry> entries = rawData.index.getAll();

        for(final IndexEntry entry : entries) {
            final IStrategoAppl key = (IStrategoAppl) entry.key;
            final String kind = key.getConstructor().getName();
            data.kinds.add(kind);
            ++data.numEntries;

            final IStrategoTerm uri = key.getSubterm(0);
            if(Tools.isTermAppl(uri) && Tools.hasConstructor((IStrategoAppl) uri, "URI", 2)) {
                final IStrategoTerm segments = uri.getSubterm(1);
                final long length = segments.getSubtermCount();
                data.uriLength.add(length);
                addToList(data.uriLengthPerKind, kind, length);

                for(IStrategoTerm segment : segments) {
                    final String segmentKind = ((IStrategoAppl) segment).getConstructor().getName();
                    data.uriSegmentKinds.add(segmentKind);
                }
            }
        }

        data.numPartitions = Iterables.size(rawData.index.getAllSources());

        data.entriesAdded = rawData.debug.indexEntriesAdded;
        data.entriesRemoved = rawData.debug.indexEntriesRemoved;

        data.diskSize = rawData.indexFile.getContent().getSize() / 1000000.0;
        data.memSize = RamUsageEstimator.sizeOf(rawData.index) / 1000000.0;

        return data;
    }

    private TaskEngineData processTaskEngineData(CollectedData rawData) throws FileSystemException {
        final TaskEngineData data = new TaskEngineData();
        final Iterable<ITask> tasks = rawData.taskEngine.getTasks();

        for(final ITask task : tasks) {
            final IStrategoTerm taskID = rawData.taskEngine.getTaskID(task);
            final String kind = task.initialInstruction().getConstructor().getName();

            data.instructionKinds.add(kind);
            ++data.numTasks;

            data.evaluations.add((long) task.evaluations());
            addToList(data.evaluationsPerKind, kind, task.evaluations());

            data.evaluationTimes.add(task.time());
            addToList(data.evaluationTimesPerKind, kind, task.time());

            data.taskKinds.add(task.type().name());

            processDependencyData(rawData.taskEngine.getDependencies(taskID, false),
                rawData.taskEngine.getDependents(taskID, false), data.staticDependencies,
                getOrCreate(data.staticDependenciesPerKind, kind, new TaskDependencyData()));
            processDependencyData(rawData.taskEngine.getDynamicDependencies(taskID),
                rawData.taskEngine.getDynamicDependents(taskID), data.dynamicDependencies,
                getOrCreate(data.dynamicDependenciesPerKind, kind, new TaskDependencyData()));
            if(processDependencyData(rawData.taskEngine.getDependencies(taskID, true),
                rawData.taskEngine.getDependents(taskID, true), data.allDependencies,
                getOrCreate(data.allDependenciesPerKind, kind, new TaskDependencyData()))) {
                processDependencyTrails(data, kind, rawData.taskEngine, taskID);
            }
            data.dependencyKind.add("Static", (int) data.staticDependencies.size);
            data.dependencyKind.add("Dynamic", (int) data.dynamicDependencies.size);

            data.taskStatusKinds.add(task.status().name());
            final long numResults = task.results().size();
            data.numResults.add(numResults);
            addToList(data.numResultsPerKind, kind, numResults);
        }

        data.numSources = Iterables.size(rawData.taskEngine.getAllSources());

        data.tasksRemoved = rawData.debug.tasksRemoved;
        data.tasksAdded = rawData.debug.tasksAdded;
        data.tasksInvalidated = rawData.debug.tasksInvalidated;
        data.evaluatedTasks = rawData.debug.evaluatedTasks.getSubtermCount();
        data.skippedTasks = rawData.debug.skippedTasks.getSubtermCount();
        data.unevaluatedTasks = rawData.debug.unevaluatedTasks.getSubtermCount();

        data.diskSize = rawData.taskEngineFile.getContent().getSize() / 1000000.0;
        data.memSize = RamUsageEstimator.sizeOf(rawData.taskEngine) / 1000000.0;

        return data;
    }

    @SuppressWarnings("unused") private boolean processDependencyData(Iterable<IStrategoTerm> outDeps,
        final Iterable<IStrategoTerm> inDeps, TaskDependencyData deps, TaskDependencyData depsPerKind) {
        boolean hasOutDeps = false;
        boolean hasInDeps = false;
        for(IStrategoTerm dep : outDeps) {
            hasOutDeps = true;
            ++deps.size;
            ++deps.inDeps;
            ++deps.outDeps;
            ++depsPerKind.size;
            ++depsPerKind.outDeps;
        }
        for(IStrategoTerm dep : inDeps) {
            hasInDeps = true;
            ++depsPerKind.inDeps;
        }
        if(hasOutDeps && hasInDeps) {
            deps.status.add("Node");
            depsPerKind.status.add("Node");
        } else if(hasOutDeps) {
            deps.status.add("Root");
            depsPerKind.status.add("Root");
            return true;
        } else if(hasInDeps) {
            deps.status.add("Leaf");
            depsPerKind.status.add("Leaf");
        } else {
            deps.status.add("Independent");
            depsPerKind.status.add("Independent");
            return true;
        }
        return false;
    }

    private void
        processDependencyTrails(TaskEngineData data, String kind, ITaskEngine taskEngine, IStrategoTerm taskID) {
        processDependencyTrails(data, kind, taskEngine, taskID, ImmutableSet.<IStrategoTerm>of());
    }

    private void processDependencyTrails(TaskEngineData data, String kind, ITaskEngine taskEngine,
        IStrategoTerm taskID, ImmutableSet<IStrategoTerm> trail) {
        trail = ImmutableSet.<IStrategoTerm>builder().addAll(trail).add(taskID).build();
        boolean hasDeps = false;
        for(IStrategoTerm dep : taskEngine.getDependencies(taskID, true)) {
            if(trail.contains(dep))
                continue; // Cycle!
            hasDeps = true;
            processDependencyTrails(data, kind, taskEngine, dep, trail);
        }
        if(!hasDeps) {
            final long size = trail.size();
            data.dependencyTrailLength.add(size);
            addToList(data.dependencyTrailLengthPerKind, kind, size);
        }
    }

    private <K, V> V getOrCreate(Map<K, V> map, K key, V defValue) {
        final V val = map.get(key);
        if(val == null) {
            map.put(key, defValue);
            return defValue;
        }
        return val;
    }

    private void addToList(Map<String, MathLongList> map, String kind, long val) {
        final MathLongList list = getOrCreate(map, kind, new MathLongList());
        list.add(val);
    }

    @SuppressWarnings("unused") private void addToList(Map<String, MathDoubleList> map, String kind, double val) {
        final MathDoubleList list = getOrCreate(map, kind, new MathDoubleList());
        list.add(val);
    }
}
