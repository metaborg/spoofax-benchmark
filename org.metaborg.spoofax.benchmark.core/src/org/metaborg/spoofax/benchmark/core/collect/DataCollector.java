package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.metaborg.runtime.task.engine.ITaskEngine;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.ITermFactory;

import com.beust.jcommander.JCommander;

public final class DataCollector {
	private final String languageDir;
	private final String languageName;
	private final String projectDir;
	private final IOAgent agent;
	private final ITermFactory termFactory;

	public DataCollector(String languageDir, String languageName, String projectDir, IOAgent agent,
		ITermFactory termFactory) {
		this.languageDir = languageDir;
		this.languageName = languageName;
		this.projectDir = projectDir;
		this.agent = agent;
		this.termFactory = termFactory;

		// Setup sunshine
		org.metaborg.sunshine.drivers.Main.jc = new JCommander();
		String[] sunshineArgs =
			new String[] { "--project", projectDir, "--auto-lang", languageDir, "--observer", "analysis-default-cmd",
				"--non-incremental" };
		SunshineMainArguments params = new SunshineMainArguments();
		final boolean argsFine = org.metaborg.sunshine.drivers.Main.parseArguments(sunshineArgs, params);
		if(!argsFine) {
			System.exit(1);
		}
		params.validate();
		org.metaborg.sunshine.drivers.Main.initEnvironment(params);
	}

	public RawData collect() {
		IndexManager.getInstance().unloadIndex(projectDir, agent);
		TaskManager.getInstance().unloadTaskEngine(projectDir, agent);

		final ServiceRegistry services = ServiceRegistry.INSTANCE();
		final AnalysisService analyzer = services.getService(AnalysisService.class);

		final Collection<File> files =
			FileUtils.listFiles(new File(projectDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

		final Collection<AnalysisResult> analyzerResults = analyzer.analyze(files);
		final IIndex index = IndexManager.getInstance().loadIndex(projectDir, languageName, termFactory, agent);
		final ITaskEngine taskEngine = TaskManager.getInstance().loadTaskEngine(projectDir, termFactory, agent);

		return new RawData(index, taskEngine, 0); // TODO: time
	}
}
