package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;

import org.metaborg.spoofax.benchmark.core.util.MathLongList;

public final class TimeData implements Serializable {
	private static final long serialVersionUID = -306568069539610307L;


	public final MathLongList parse = new MathLongList();
	public final MathLongList preTrans = new MathLongList();
	public final MathLongList collect = new MathLongList();
	public final MathLongList taskEval = new MathLongList();
	public final MathLongList postTrans = new MathLongList();
	public final MathLongList indexPersist = new MathLongList();
	public final MathLongList taskPersist = new MathLongList();


	public double totalByMean() {
		return parse.mean() + preTrans.mean() + collect.mean() + taskEval.mean() + postTrans.mean()
			+ indexPersist.mean() + taskPersist.mean();
	}
}
