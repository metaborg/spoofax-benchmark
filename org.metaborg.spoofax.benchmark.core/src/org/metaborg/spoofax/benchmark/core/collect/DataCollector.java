package org.metaborg.spoofax.benchmark.core.collect;

import java.io.File;
import java.util.Collection;
import java.util.List;

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
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterables;

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

	public CollectedData collect(int warmupPhases, int measurementPhases) {
		final ServiceRegistry services = ServiceRegistry.INSTANCE();
		final AnalysisService analyzer = services.getService(AnalysisService.class);
		final LanguageService languages = services.getService(LanguageService.class);

		final IOFileFilter extensionFilter =
			createExtensionFilter(languages.getLanguageByName(languageName).getFileExtensions());
		final Collection<File> files =
			FileUtils.listFiles(new File(projectDir), extensionFilter, TrueFileFilter.INSTANCE);

		for(int i = 0; i < warmupPhases; ++i) {
			analyze(analyzer, files);
		}

		final List<Collection<AnalysisResult>> allResults = Lists.newLinkedList();
		for(int i = 0; i < measurementPhases; ++i) {
			allResults.add(analyze(analyzer, files));
		}
		if(allResults.size() == 0)
			throw new RuntimeException("Could not analyze files.");

		final List<IStrategoTerm> rawResults = Lists.newLinkedList();
		for(Collection<AnalysisResult> analyzerResults : allResults) {
			rawResults.add(Iterables.getFirst(analyzerResults, null).rawResults());
		}
		final Collection<AnalysisResult> firstAnalyzerResults = allResults.get(0);
		final IStrategoTerm firstRawResult = rawResults.get(0);

		final CollectedData data = new CollectedData();
		data.languageDirectory = languageDir;
		data.languageName = languageName;
		data.projectDirectory = projectDir;

		for(final AnalysisResult result : firstAnalyzerResults) {
			final FileData fileData = new FileData();
			fileData.name = result.file().getAbsolutePath();
			fileData.ast = result.ast();
			for(IMessage message : result.messages()) {
				fileData.messages.add(message.message());
			}
			data.files.add(fileData);
		}

		final IndexManager indexManager = IndexManager.getInstance();
		data.indexFile = indexManager.getIndexFile(indexManager.getProjectURI(projectDir, agent)).getAbsolutePath();
		data.index = indexManager.loadIndex(projectDir, languageName, termFactory, agent);
		data.indexEntriesAdded = Tools.asJavaInt(firstRawResult.getSubterm(1).getSubterm(0));
		data.indexEntriesRemoved = Tools.asJavaInt(firstRawResult.getSubterm(1).getSubterm(1));

		final TaskManager taskManager = TaskManager.getInstance();
		data.taskEngineFile =
			taskManager.getTaskEngineFile(taskManager.getProjectURI(projectDir, agent)).getAbsolutePath();
		data.taskEngine = taskManager.loadTaskEngine(projectDir, termFactory, agent);
		data.tasksRemoved = Tools.asJavaInt(firstRawResult.getSubterm(2).getSubterm(0));
		data.tasksAdded = Tools.asJavaInt(firstRawResult.getSubterm(2).getSubterm(1));
		data.tasksInvalidated = Tools.asJavaInt(firstRawResult.getSubterm(2).getSubterm(2));
		data.evaluatedTasks = firstRawResult.getSubterm(2).getSubterm(3).getSubtermCount();
		data.skippedTasks = firstRawResult.getSubterm(2).getSubterm(4).getSubtermCount();
		data.unevaluatedTasks = firstRawResult.getSubterm(2).getSubterm(5).getSubtermCount();

		for(final IStrategoTerm rawResult : rawResults) {
			data.time.parse.add(Tools.asJavaDouble(rawResult.getSubterm(3).getSubterm(0)));
			data.time.collect.add(Tools.asJavaDouble(rawResult.getSubterm(3).getSubterm(1)));
			data.time.taskEval.add(Tools.asJavaDouble(rawResult.getSubterm(3).getSubterm(2)));
			data.time.indexPersist.add(Tools.asJavaDouble(rawResult.getSubterm(3).getSubterm(3)));
			data.time.taskPersist.add(Tools.asJavaDouble(rawResult.getSubterm(3).getSubterm(4)));
		}

		return data;
	}

	private Collection<AnalysisResult> analyze(AnalysisService analyzer, Collection<File> files) {
		resetIndex();
		resetTaskEngine();
		return analyzer.analyze(files);
	}

	private IOFileFilter createExtensionFilter(Collection<String> extensions) {
		final IOFileFilter[] filters = new IOFileFilter[extensions.size()];
		int i = 0;
		for(String extension : extensions) {
			filters[i++] = FileFilterUtils.suffixFileFilter(extension);
		}
		return FileFilterUtils.or(filters);
	}

	private void resetIndex() {
		final IndexManager indexManager = IndexManager.getInstance();
		indexManager.unloadIndex(projectDir, agent);
		indexManager.getIndexFile(indexManager.getProjectURI(projectDir, agent)).delete();
	}

	private void resetTaskEngine() {
		final TaskManager taskManager = TaskManager.getInstance();
		taskManager.unloadTaskEngine(projectDir, agent);
		taskManager.getTaskEngineFile(taskManager.getProjectURI(projectDir, agent)).delete();
	}
}
