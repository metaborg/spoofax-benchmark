package org.metaborg.spoofax.benchmark.core.collect;

import java.io.Serializable;
import java.util.Collection;

import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.spoofax.core.analysis.AnalysisDebugResult;
import org.spoofax.interpreter.library.index.IIndex;

import com.google.common.collect.Lists;

public final class CollectedData implements Serializable {
	private static final long serialVersionUID = 5478160852945519151L;

	public String languageDirectory;
	public String languageName;
	public String projectDirectory;

	public final Collection<FileData> files = Lists.newLinkedList();

	public transient String indexFile;
	public transient IIndex index;

	public transient String taskEngineFile;
	public transient ITaskEngine taskEngine;

	public AnalysisDebugResult debug;
	public final TimeData time = new TimeData();
}
