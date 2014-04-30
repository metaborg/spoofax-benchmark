package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class TaskEngineData implements Serializable {
	private static final long serialVersionUID = -2134416235883835839L;

	
	public long numTasks;
	public Multiset<String> numKinds = HashMultiset.create();

	public long numSources;

	public long tasksRemoved;
	public long tasksAdded;
	public long tasksInvalidated;
	public long evaluatedTasks;
	public long skippedTasks;
	public long unevaluatedTasks;

	public long memSize;
	public long diskSize;
}
