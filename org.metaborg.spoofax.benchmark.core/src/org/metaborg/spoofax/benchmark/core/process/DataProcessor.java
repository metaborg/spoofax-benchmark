package org.metaborg.spoofax.benchmark.core.process;

import java.util.Map;

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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class DataProcessor {
	public ProcessedData process(CollectedData rawData) {
		final TimeData timeData = processTimeData(rawData);
		final IndexData indexData = processIndexData(rawData);
		final TaskEngineData taskEngineData = processTaskEngineData(rawData);

		return new ProcessedData(timeData, indexData, taskEngineData);
	}

	private TimeData processTimeData(CollectedData rawData) {
		return rawData.time;
	}

	private IndexData processIndexData(CollectedData rawData) {
		final IndexData data = new IndexData();
		final Iterable<IndexEntry> entries = rawData.index.getAll();

		for(final IndexEntry entry : entries) {
			final String kind = entry.getKey().getConstructor().getName();
			data.kinds.add(kind);
			++data.numEntries;

			final IStrategoTerm identifier = entry.getKey().getIdentifier();
			if(Tools.isTermAppl(identifier) && Tools.hasConstructor((IStrategoAppl) identifier, "URI", 2)) {
				final IStrategoTerm segments = identifier.getSubterm(1);
				final long length = segments.getSubtermCount();
				data.uriLength.add(length);
				addToList(data.uriLengthPerKind, kind, length);

				for(IStrategoTerm segment : segments) {
					final String segmentKind = ((IStrategoAppl) segment).getConstructor().getName();
					data.uriSegmentKinds.add(segmentKind);
				}
			}
		}

		data.numPartitions = Iterables.size(rawData.index.getAllPartitions());

		data.entriesAdded = rawData.indexEntriesAdded;
		data.entriesRemoved = rawData.indexEntriesRemoved;

		// TODO: memory and disk sizes.

		return data;
	}

	private TaskEngineData processTaskEngineData(CollectedData rawData) {
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

			data.taskTypes.add(task.type().name());

			if(processDependencyData(data, rawData.taskEngine.getDependencies(taskID, false),
				rawData.taskEngine.getDependents(taskID, false),
				getOrCreate(data.dependenciesPerKind, kind, new TaskDependencyData()))) {
				processDependencyTrails(data, kind, rawData.taskEngine, taskID);
			}
			processDependencyData(data, rawData.taskEngine.getDynamicDependencies(taskID),
				rawData.taskEngine.getDynamicDependents(taskID),
				getOrCreate(data.dynamicDependenciesPerKind, kind, new TaskDependencyData()));

			data.taskStatusKinds.add(task.status().name());
			final long numResults = task.results().size();
			data.numResults.add(numResults);
			addToList(data.numResultsPerKind, kind, numResults);
		}

		data.numSources = Iterables.size(rawData.taskEngine.getAllSources());

		data.tasksRemoved = rawData.tasksRemoved;
		data.tasksAdded = rawData.tasksAdded;
		data.tasksInvalidated = rawData.tasksInvalidated;
		data.evaluatedTasks = rawData.evaluatedTasks;
		data.skippedTasks = rawData.skippedTasks;
		data.unevaluatedTasks = rawData.unevaluatedTasks;

		// TODO: memory and disk sizes.

		return data;
	}

	@SuppressWarnings("unused")
	private boolean processDependencyData(TaskEngineData data, Iterable<IStrategoTerm> outDeps,
		final Iterable<IStrategoTerm> inDeps, TaskDependencyData depsPerKind) {
		boolean hasOutDeps = false;
		boolean hasInDeps = false;
		for(IStrategoTerm dep : outDeps) {
			hasOutDeps = true;
			++data.dependencies.size;
			++data.dependencies.inDeps;
			++data.dependencies.outDeps;
			++depsPerKind.size;
			++depsPerKind.outDeps;
		}
		for(IStrategoTerm dep : inDeps) {
			hasInDeps = true;
			++depsPerKind.inDeps;
		}
		if(hasOutDeps && hasInDeps) {
			data.dependencies.kind.add("Node");
			depsPerKind.kind.add("Node");
		} else if(hasOutDeps) {
			data.dependencies.kind.add("Root");
			depsPerKind.kind.add("Root");
			return true;
		} else if(hasInDeps) {
			data.dependencies.kind.add("Leaf");
			depsPerKind.kind.add("Leaf");
		} else {
			data.dependencies.kind.add("Independent");
			depsPerKind.kind.add("Independent");
			return true;
		}
		return false;
	}

	private void
		processDependencyTrails(TaskEngineData data, String kind, ITaskEngine taskEngine, IStrategoTerm taskID) {
		processDependencyTrails(data, kind, taskEngine, taskID, ImmutableSet.<IStrategoTerm> of());
	}

	private void processDependencyTrails(TaskEngineData data, String kind, ITaskEngine taskEngine,
		IStrategoTerm taskID, ImmutableSet<IStrategoTerm> trail) {
		trail = ImmutableSet.<IStrategoTerm> builder().addAll(trail).add(taskID).build();
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

	@SuppressWarnings("unused")
	private void addToList(Map<String, MathDoubleList> map, String kind, double val) {
		final MathDoubleList list = getOrCreate(map, kind, new MathDoubleList());
		list.add(val);
	}
}
