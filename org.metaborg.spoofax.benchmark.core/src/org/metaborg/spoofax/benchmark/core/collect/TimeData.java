package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;

import org.metaborg.spoofax.benchmark.core.util.MathDoubleList;

public final class TimeData implements Serializable {
	private static final long serialVersionUID = -306568069539610307L;


	public final MathDoubleList parse = new MathDoubleList();
	public final MathDoubleList preTrans = new MathDoubleList();
	public final MathDoubleList collect = new MathDoubleList();
	public final MathDoubleList taskEval = new MathDoubleList();
	public final MathDoubleList postTrans = new MathDoubleList();
	public final MathDoubleList indexPersist = new MathDoubleList();
	public final MathDoubleList taskPersist = new MathDoubleList();


	public double totalByMean() {
		return parse.mean() + preTrans.mean() + collect.mean() + taskEval.mean() + postTrans.mean()
			+ indexPersist.mean() + taskPersist.mean();
	}
}
