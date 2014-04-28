package org.metaborg.spoofax.benchmark.core.process;

import java.util.Map;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

public final class IndexData {
	public long numEntries;
	public Multiset<String> numKinds = HashMultiset.create();

	public long numPartitions;

	public long memSize;
	public long diskSize;
}
