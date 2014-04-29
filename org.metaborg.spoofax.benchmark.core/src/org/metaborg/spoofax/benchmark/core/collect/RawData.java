package org.metaborg.spoofax.benchmark.core.collect;

import java.util.Collection;

import org.metaborg.runtime.task.engine.ITaskEngine;
import org.spoofax.interpreter.library.index.IIndex;

import com.google.common.collect.Lists;

public final class RawData {
	public final Collection<FileData> files = Lists.newLinkedList();

	public IIndex index;
	public long indexEntriesRemoved;
	public long indexEntriesAdded;

	public ITaskEngine taskEngine;
	public long tasksRemoved;
	public long tasksAdded;
	public long tasksInvalidated;
	public long evaluatedTasks;
	public long skippedTasks;
	public long unevaluatedTasks;

	public double parseTime;
	public double collectTime;
	public double performTime;
	public double indexPersistTime;
	public double taskPersistTime;
}
