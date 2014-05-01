package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;
import java.util.Map;

import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class IndexData implements Serializable {
	private static final long serialVersionUID = 2830783095447774209L;

	public long numEntries;
	public Multiset<String> kinds = HashMultiset.create();

	public long numPartitions;

	public long entriesRemoved;
	public long entriesAdded;

	public final MathLongList uriLength = new MathLongList();
	public final Map<String, MathLongList> uriLengthPerKind = Maps.newHashMap();
	public final Multiset<String> uriSegmentKinds = HashMultiset.create();

	public double memSize;
	public double diskSize;
}
