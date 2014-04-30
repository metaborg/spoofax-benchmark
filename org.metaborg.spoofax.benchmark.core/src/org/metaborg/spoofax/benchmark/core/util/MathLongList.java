package org.metaborg.spoofax.benchmark.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MathLongList extends ArrayList<Long> {
	private static final long serialVersionUID = -843770397648050526L;


	public MathLongList() {
		super();
	}

	public MathLongList(Collection<? extends Long> c) {
		super(c);
	}


	public long sum() {
		Long sum = 0L;
		for(Long v : this) {
			sum += v;
		}
		return sum;
	}

	public long min() {
		Long min = Long.MAX_VALUE;
		for(Long v : this) {
			if(v < min)
				min = v;
		}
		return min;
	}

	public long max() {
		Long max = Long.MIN_VALUE;
		for(Long v : this) {
			if(v > max)
				max = v;
		}
		return max;
	}

	public double mean() {
		return (double) sum() / this.size();
	}

	public double median() {
		final ArrayList<Long> list = new ArrayList<Long>(this);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if(list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public double sdev() {
		final MathLongList list = new MathLongList(this);
		Collections.sort(list);
		int sum = 0;
		double mean = list.mean();

		for(Long i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}
}
