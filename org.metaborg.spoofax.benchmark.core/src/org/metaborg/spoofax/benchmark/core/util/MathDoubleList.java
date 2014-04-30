package org.metaborg.spoofax.benchmark.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MathDoubleList extends ArrayList<Double> {
	private static final long serialVersionUID = 8402460541108285068L;


	public MathDoubleList() {
		super();
	}

	public MathDoubleList(Collection<? extends Double> c) {
		super(c);
	}


	public double sum() {
		Double sum = 0.0;
		for(Double v : this) {
			sum += v;
		}
		return sum;
	}

	public double min() {
		Double min = Double.MAX_VALUE;
		for(Double v : this) {
			if(v < min)
				min = v;
		}
		return min;
	}

	public double max() {
		Double max = Double.MIN_VALUE;
		for(Double v : this) {
			if(v > max)
				max = v;
		}
		return max;
	}

	public double mean() {
		return sum() / this.size();
	}

	public double median() {
		final ArrayList<Double> list = new ArrayList<Double>(this);
		Collections.sort(list);

		int middle = (int) Math.floor(((double) list.size()) / 2);

		if(list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public double sdev() {
		final MathDoubleList list = new MathDoubleList(this);
		Collections.sort(list);
		int sum = 0;
		double mean = list.mean();

		for(Double i : list)
			sum += Math.pow((i - mean), 2);
		return list.size() == 1 ? 0 : Math.sqrt(sum / (list.size() - 1));
	}
}
