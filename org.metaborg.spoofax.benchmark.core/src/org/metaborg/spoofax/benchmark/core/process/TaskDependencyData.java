package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class TaskDependencyData implements Serializable {
	private static final long serialVersionUID = 4937968667415495372L;

	public long size;
	public long inDeps;
	public long outDeps;

	public final Multiset<String> kind = HashMultiset.create();
}
