package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;

public final class TimeData implements Serializable {
	private static final long serialVersionUID = -306568069539610307L;
	
	
	public double parse;
	public double collect;
	public double taskEval;
	public double indexPersist;
	public double taskPersist;
	
	
	public double total() {
		return parse + collect + taskEval + indexPersist + taskPersist;
	}
}
