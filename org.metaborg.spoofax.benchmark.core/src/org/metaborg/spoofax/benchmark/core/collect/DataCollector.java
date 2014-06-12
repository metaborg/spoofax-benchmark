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
import org.metaborg.sunshine.services.parser.ParserService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.attachments.TermAttachmentStripper;

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
		final LanguageService languages = services.getService(LanguageService.class);
		final ParserService parser = services.getService(ParserService.class);
		final AnalysisService analyzer = services.getService(AnalysisService.class);

		final IOFileFilter extensionFilter =
			createExtensionFilter(languages.getLanguageByName(languageName).getFileExtensions());
		final Collection<File> files =
			FileUtils.listFiles(new File(projectDir), extensionFilter, TrueFileFilter.INSTANCE);

		for(int i = 0; i < warmupPhases; ++i) {
			analyze(parser, analyzer, files);
		}

		final List<Collection<AnalysisResult>> allResults = Lists.newLinkedList();
		for(int i = 0; i < measurementPhases; ++i) {
			allResults.add(analyze(parser, analyzer, files));
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

		final TermAttachmentStripper attachmentStripper = new TermAttachmentStripper(termFactory);
		for(final AnalysisResult result : firstAnalyzerResults) {
			final FileData fileData = new FileData();
			fileData.name = result.file().getAbsolutePath();
			fileData.ast = attachmentStripper.strip(result.ast());
			for(IMessage message : result.messages()) {
				fileData.messages.add(message.message());
			}
			data.files.add(fileData);
		}

		final IStrategoTerm firstDebugResult = firstRawResult.getSubterm(2);
		final IStrategoTerm firstCollectDebugResult = firstDebugResult.getSubterm(0);

		final IndexManager indexManager = IndexManager.getInstance();
		data.indexFile = indexManager.getIndexFile(indexManager.getProjectURI(projectDir, agent)).getAbsolutePath();
		data.index = indexManager.loadIndex(projectDir, languageName, termFactory, agent);
		data.indexEntriesRemoved = Tools.asJavaInt(firstCollectDebugResult.getSubterm(0));
		data.indexEntriesAdded = Tools.asJavaInt(firstCollectDebugResult.getSubterm(1));

		final TaskManager taskManager = TaskManager.getInstance();
		data.taskEngineFile =
			taskManager.getTaskEngineFile(taskManager.getProjectURI(projectDir, agent)).getAbsolutePath();
		data.taskEngine = taskManager.loadTaskEngine(projectDir, termFactory, agent);
		data.tasksRemoved = Tools.asJavaInt(firstCollectDebugResult.getSubterm(0));
		data.tasksAdded = Tools.asJavaInt(firstCollectDebugResult.getSubterm(1));
		data.tasksInvalidated = Tools.asJavaInt(firstCollectDebugResult.getSubterm(2));
		data.evaluatedTasks = firstDebugResult.getSubterm(1).getSubtermCount();
		data.skippedTasks = firstDebugResult.getSubterm(2).getSubtermCount();
		data.unevaluatedTasks = firstDebugResult.getSubterm(3).getSubtermCount();

		for(final IStrategoTerm rawResult : rawResults) {
			final IStrategoTerm timeResult = rawResult.getSubterm(3);
			data.time.parse.add((long) Tools.asJavaDouble(timeResult.getSubterm(0)));
			data.time.preTrans.add((long) Tools.asJavaDouble(timeResult.getSubterm(1)));
			data.time.collect.add((long) Tools.asJavaDouble(timeResult.getSubterm(2)));
			data.time.taskEval.add((long) Tools.asJavaDouble(timeResult.getSubterm(3)));
			data.time.postTrans.add((long) Tools.asJavaDouble(timeResult.getSubterm(4)));
			data.time.indexPersist.add((long) Tools.asJavaDouble(timeResult.getSubterm(5)));
			data.time.taskPersist.add((long) Tools.asJavaDouble(timeResult.getSubterm(6)));
		}

		return data;
	}

	private Collection<AnalysisResult> analyze(ParserService parser, AnalysisService analyzer, Collection<File> files) {
		resetIndex();
		resetTaskEngine();

		final Collection<AnalysisResult> parseResults = Lists.newLinkedList();
		for(File file : files) {
			parseResults.add(parser.parseFile(file));
		}

		return analyzer.analyze(parseResults);
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
