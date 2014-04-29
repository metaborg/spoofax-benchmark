package org.metaborg.spoofax.benchmark.core.process;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class IndexData {
	public long numEntries;
	public Multiset<String> numKinds = HashMultiset.create();

	public long numPartitions;

	public long indexEntriesRemoved;
	public long indexEntriesAdded;

	public long memSize;
	public long diskSize;
}
