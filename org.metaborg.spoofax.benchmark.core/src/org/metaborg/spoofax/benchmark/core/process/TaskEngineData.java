package org.metaborg.spoofax.benchmark.core.process;

import java.io.Serializable;
import java.util.Map;

import org.metaborg.spoofax.benchmark.core.util.MathLongList;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public final class TaskEngineData implements Serializable {
	private static final long serialVersionUID = -2134416235883835839L;

	public long numTasks;
	public Multiset<String> instructionKinds = HashMultiset.create();

	public long numSources;

	public long tasksRemoved;
	public long tasksAdded;
	public long tasksInvalidated;

	public long evaluatedTasks;
	public long skippedTasks;
	public long unevaluatedTasks;

	public double cycleFixpointTime;
	public long cycleFixpointRounds;

	public final MathLongList evaluations = new MathLongList();
	public final Map<String, MathLongList> evaluationsPerKind = Maps.newHashMap();

	public final MathLongList evaluationTimes = new MathLongList();
	public final Map<String, MathLongList> evaluationTimesPerKind = Maps.newHashMap();

	public final Multiset<String> taskKinds = HashMultiset.create();

	public final TaskDependencyData staticDependencies = new TaskDependencyData();
	public final Map<String, TaskDependencyData> staticDependenciesPerKind = Maps.newHashMap();
	public final TaskDependencyData dynamicDependencies = new TaskDependencyData();
	public final Map<String, TaskDependencyData> dynamicDependenciesPerKind = Maps.newHashMap();
	public final TaskDependencyData allDependencies = new TaskDependencyData();
	public final Map<String, TaskDependencyData> allDependenciesPerKind = Maps.newHashMap();
	public final MathLongList dependencyTrailLength = new MathLongList();
	public final Map<String, MathLongList> dependencyTrailLengthPerKind = Maps.newHashMap();
	public final Multiset<String> dependencyKind = HashMultiset.create();
	
	public final Multiset<String> taskStatusKinds = HashMultiset.create();
	public final MathLongList numResults = new MathLongList();
	public final Map<String, MathLongList> numResultsPerKind = Maps.newHashMap();

	public double memSize;
	public double diskSize;
}
