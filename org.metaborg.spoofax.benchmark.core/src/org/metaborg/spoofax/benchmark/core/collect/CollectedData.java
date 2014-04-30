package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;
import java.util.Collection;

import org.metaborg.runtime.task.engine.ITaskEngine;
import org.spoofax.interpreter.library.index.IIndex;

import com.google.common.collect.Lists;

public final class CollectedData implements Serializable {
	private static final long serialVersionUID = 5478160852945519151L;

	public final Collection<FileData> files = Lists.newLinkedList();

	public transient IIndex index;
	public long indexEntriesRemoved;
	public long indexEntriesAdded;

	public transient ITaskEngine taskEngine;
	public long tasksRemoved;
	public long tasksAdded;
	public long tasksInvalidated;
	public long evaluatedTasks;
	public long skippedTasks;
	public long unevaluatedTasks;

	public final TimeData time = new TimeData();
}
