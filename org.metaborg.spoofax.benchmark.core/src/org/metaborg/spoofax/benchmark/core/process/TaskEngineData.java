package org.metaborg.spoofax.benchmark.core.process;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class TaskEngineData {
	public long numTasks;
	public Multiset<String> numKinds = HashMultiset.create();

	public long numSources;

	public long memSize;
	public long diskSize;
}
