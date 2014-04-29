package org.metaborg.spoofax.benchmark.core.process;

import org.metaborg.runtime.task.ITask;
import org.metaborg.spoofax.benchmark.core.collect.RawData;
import org.spoofax.interpreter.library.index.IndexEntry;

import com.google.common.collect.Iterables;

public final class DataProcessor {
	public ProcessedData process(RawData rawData) {
		final TimeData timeData = processTimeData(rawData);
		final IndexData indexData = processIndexData(rawData);
		final TaskEngineData taskEngineData = processTaskEngineData(rawData);

		return new ProcessedData(timeData, indexData, taskEngineData);
	}

	private TimeData processTimeData(RawData rawData) {
		final TimeData data = new TimeData();
		
		data.parseTime = rawData.parseTime;
		data.collectTime = rawData.collectTime;
		data.performTime = rawData.performTime;
		data.indexPersistTime = rawData.indexPersistTime;
		data.taskPersistTime = rawData.taskPersistTime;
		
		return data;
	}

	private IndexData processIndexData(RawData rawData) {
		final IndexData data = new IndexData();
		final Iterable<IndexEntry> entries = rawData.index.getAll();

		for(final IndexEntry entry : entries) {
			data.numKinds.add(entry.getKey().getConstructor().getName());
			++data.numEntries;
		}

		data.numPartitions = Iterables.size(rawData.index.getAllPartitions());
		
		data.indexEntriesAdded = rawData.indexEntriesAdded;
		data.indexEntriesRemoved = rawData.indexEntriesRemoved;

		// TODO: memory and disk sizes.

		return data;
	}

	private TaskEngineData processTaskEngineData(RawData rawData) {
		final TaskEngineData data = new TaskEngineData();
		final Iterable<ITask> tasks = rawData.taskEngine.getTasks();

		for(final ITask task : tasks) {
			data.numKinds.add(task.initialInstruction().getConstructor().getName());
			++data.numTasks;
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
}
