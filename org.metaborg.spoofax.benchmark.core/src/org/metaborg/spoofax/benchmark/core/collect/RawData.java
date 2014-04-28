package org.metaborg.spoofax.benchmark.core.collect;

import org.metaborg.runtime.task.engine.ITaskEngine;
import org.spoofax.interpreter.library.index.IIndex;

public class RawData {
	public final IIndex index;
	public final ITaskEngine taskEngine;
	public final long time;
	
	public RawData(IIndex index, ITaskEngine taskEngine, long time) {
		super();
		this.index = index;
		this.taskEngine = taskEngine;
		this.time = time;
	}
}
