package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;

import org.metaborg.spoofax.benchmark.core.collect.TimeData;

public final class ProcessedData implements Serializable {
	private static final long serialVersionUID = 19280937264478265L;
	
	public final TimeData time;
	public final IndexData index;
	public final TaskEngineData taskEngine;
	
	public ProcessedData() {
		this.time = new TimeData();
		this.index = new IndexData();
		this.taskEngine = new TaskEngineData();
	}
	
	public ProcessedData(TimeData time, IndexData index, TaskEngineData taskEngine) {
		this.time = time;
		this.index = index;
		this.taskEngine = taskEngine;
	}
}
