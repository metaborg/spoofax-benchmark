package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;
import java.util.Map;

import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.beust.jcommander.internal.Maps;

public final class TimeData implements Serializable {
	private static final long serialVersionUID = -306568069539610307L;

	public final Map<String, MathLongList> map = Maps.newHashMap();

	public void add(String kind, long time) {
		MathLongList list = this.map.get(kind);
		if(list == null) {
			list = new MathLongList();
			this.map.put(kind, list);
		}
		list.add(time);
	}

	public double totalByMean() {
		double totalTime = 0.0;
		for(MathLongList timeList : this.map.values()) {
			totalTime += timeList.mean();
		}
		return totalTime;
	}
}
