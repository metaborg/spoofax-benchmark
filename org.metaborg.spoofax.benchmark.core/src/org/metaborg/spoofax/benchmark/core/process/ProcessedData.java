package org.metaborg.spoofax.benchmark.core.process;

public final class ProcessedData {
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
