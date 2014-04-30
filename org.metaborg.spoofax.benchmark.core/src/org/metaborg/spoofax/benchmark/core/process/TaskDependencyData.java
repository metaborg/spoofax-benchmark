package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;

import org.metaborg.spoofax.benchmark.core.util.MathLongList;

public final class TaskDependencyData implements Serializable {
	private static final long serialVersionUID = 4937968667415495372L;

	public long size;
	public long inDeps;
	public long outDeps;
	public final MathLongList chainLength = new MathLongList();
	
	public long numNodes;
	public long numRoots;
	public long numLeaves;
	public long numIndependents;
}
