package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;

public final class TimeData implements Serializable {
	private static final long serialVersionUID = -306568069539610307L;
	
	
	public double parseTime;
	public double collectTime;
	public double performTime;
	public double indexPersistTime;
	public double taskPersistTime;
}
