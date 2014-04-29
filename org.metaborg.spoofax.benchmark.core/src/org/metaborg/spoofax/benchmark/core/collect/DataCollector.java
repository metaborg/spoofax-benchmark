package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.model.messages.IMessage;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
import org.metaborg.sunshine.services.language.LanguageService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.beust.jcommander.JCommander;
import com.google.common.collect.Iterables;

public final class DataCollector {
	private final String languageName;
	private final String projectDir;
	private final IOAgent agent;
	private final ITermFactory termFactory;

	public DataCollector(String languageDir, String languageName, String projectDir, IOAgent agent,
		ITermFactory termFactory) {
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
		final LanguageService languages = services.getService(LanguageService.class);

		final IOFileFilter extensionFilter =
			createExtensionFilter(languages.getLanguageByName(languageName).getFileExtensions());
		final Collection<File> files =
			FileUtils.listFiles(new File(projectDir), extensionFilter, TrueFileFilter.INSTANCE);

		final Collection<AnalysisResult> analyzerResults = analyzer.analyze(files);
		if(Iterables.isEmpty(analyzerResults))
			throw new RuntimeException("Could not analyze files.");

		final RawData data = new RawData();

		for(AnalysisResult result : analyzerResults) {
			final FileData fileData = new FileData();
			fileData.name = result.file().getAbsolutePath();
			fileData.ast = result.ast();
			for(IMessage message : result.messages()) {
				fileData.messages.add(message.message());
			}
			data.files.add(fileData);
		}
		IStrategoTerm result = Iterables.getFirst(analyzerResults, null).rawResults();

		data.index = IndexManager.getInstance().loadIndex(projectDir, languageName, termFactory, agent);
		data.indexEntriesAdded = Tools.asJavaInt(result.getSubterm(1).getSubterm(0));
		data.indexEntriesRemoved = Tools.asJavaInt(result.getSubterm(1).getSubterm(1));

		data.taskEngine = TaskManager.getInstance().loadTaskEngine(projectDir, termFactory, agent);
		data.tasksRemoved = Tools.asJavaInt(result.getSubterm(2).getSubterm(0));
		data.tasksAdded = Tools.asJavaInt(result.getSubterm(2).getSubterm(1));
		data.tasksInvalidated = Tools.asJavaInt(result.getSubterm(2).getSubterm(2));
		data.evaluatedTasks = result.getSubterm(2).getSubterm(3).getSubtermCount();
		data.skippedTasks = result.getSubterm(2).getSubterm(4).getSubtermCount();
		data.unevaluatedTasks = result.getSubterm(2).getSubterm(5).getSubtermCount();

		data.parseTime = Tools.asJavaDouble(result.getSubterm(3).getSubterm(0));
		data.collectTime = Tools.asJavaDouble(result.getSubterm(3).getSubterm(1));
		data.performTime = Tools.asJavaDouble(result.getSubterm(3).getSubterm(2));
		data.indexPersistTime = Tools.asJavaDouble(result.getSubterm(3).getSubterm(3));
		data.taskPersistTime = Tools.asJavaDouble(result.getSubterm(3).getSubterm(4));

		return data;
	}

	private IOFileFilter createExtensionFilter(Collection<String> extensions) {
		final IOFileFilter[] filters = new IOFileFilter[extensions.size()];
		int i = 0;
		for(String extension : extensions) {
			filters[i++] = FileFilterUtils.suffixFileFilter(extension);
		}
		return FileFilterUtils.or(filters);
	}
}
