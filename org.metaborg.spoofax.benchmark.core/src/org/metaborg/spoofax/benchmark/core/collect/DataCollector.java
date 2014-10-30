package org.metaborg.spoofax.benchmark.core.collect;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageFileSelector;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.parser.IParseService;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.analyzer.AnalysisFileResult;
import org.metaborg.sunshine.services.analyzer.AnalysisResult;
import org.metaborg.sunshine.services.analyzer.AnalysisService;
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

        // Make logger stfu
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        LoggerConfig logger = ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
        ctx.updateLoggers();

        // Setup sunshine
        org.metaborg.sunshine.drivers.Main.jc = new JCommander();
        final String[] sunshineArgs =
            new String[] { "--project", projectDir, "--auto-lang", languageDir, "--observer",
                "analysis-default-cmd", "--non-incremental" };
        final SunshineMainArguments params = new SunshineMainArguments();
        final boolean argsFine = org.metaborg.sunshine.drivers.Main.parseArguments(sunshineArgs, params);
        if(!argsFine) {
            System.exit(1);
        }
        params.validate();
        org.metaborg.sunshine.drivers.Main.initEnvironment(params);
    }

    public CollectedData collect(int warmupPhases, int measurementPhases) throws IOException {
        final ServiceRegistry services = ServiceRegistry.INSTANCE();
        final IResourceService resources = services.getService(IResourceService.class);
        final ILanguageService languages = services.getService(ILanguageService.class);
        final ILanguageIdentifierService identifier = services.getService(ILanguageIdentifierService.class);
        @SuppressWarnings("unchecked") final IParseService<IStrategoTerm> parser =
            services.getService(IParseService.class);
        final AnalysisService analyzer = services.getService(AnalysisService.class);

        final ILanguage language = languages.get(languageName);
        final FileObject[] files =
            resources.resolve(projectDir).findFiles(new LanguageFileSelector(identifier, language));

        for(int i = 0; i < warmupPhases; ++i) {
            analyze(parser, analyzer, language, files);
        }

        final List<AnalysisResult> allResults = Lists.newLinkedList();
        for(int i = 0; i < measurementPhases; ++i) {
            final Collection<AnalysisResult> results = analyze(parser, analyzer, language, files);
            // Since we are only analyzing files of one language, there should only be one result in results.
            allResults.add(Iterables.getFirst(results, null));
        }
        if(allResults.size() == 0)
            throw new RuntimeException("Could not analyze files.");

        final AnalysisResult firstResult = allResults.get(0);

        final CollectedData data = new CollectedData();
        data.languageDirectory = languageDir;
        data.languageName = languageName;
        data.projectDirectory = projectDir;

        final TermAttachmentStripper attachmentStripper = new TermAttachmentStripper(termFactory);
        for(final AnalysisFileResult fileResult : firstResult.fileResults) {
            final FileData fileData = new FileData();
            fileData.name = fileResult.file().getName().getPath();
            fileData.ast = attachmentStripper.strip(fileResult.ast());
            for(IMessage message : fileResult.messages()) {
                fileData.messages.add(message.message());
            }
            data.files.add(fileData);
        }

        final IndexManager indexManager = IndexManager.getInstance();
        data.indexFile =
            indexManager.getIndexFile(indexManager.getProjectURI(projectDir, agent)).getAbsolutePath();
        data.index = indexManager.loadIndex(projectDir, languageName, termFactory, agent);

        final TaskManager taskManager = TaskManager.getInstance();
        data.taskEngineFile =
            taskManager.getTaskEngineFile(taskManager.getProjectURI(projectDir, agent)).getAbsolutePath();
        data.taskEngine = taskManager.loadTaskEngine(projectDir, termFactory, agent);

        data.debug = firstResult.debugResult;

        for(final AnalysisResult result : allResults) {
            data.time.add("Parse", result.timeResult.parse);
            data.time.add("Pre-trans", result.timeResult.preTrans);
            data.time.add("Collect", result.timeResult.collect);
            data.time.add("Task eval", result.timeResult.taskEval);
            data.time.add("Post-trans", result.timeResult.postTrans);
            data.time.add("Index persist", result.timeResult.indexPersist);
            data.time.add("Task engine persist", result.timeResult.taskPersist);
        }

        return data;
    }

    private Collection<AnalysisResult> analyze(IParseService<IStrategoTerm> parser, AnalysisService analyzer,
        ILanguage language, FileObject[] files) throws IOException {
        resetIndex();
        resetTaskEngine();
        forceGC();

        final Collection<ParseResult<IStrategoTerm>> parseResults = Lists.newLinkedList();
        for(FileObject file : files) {
            parseResults.add(parser.parse(file, language));
        }

        return analyzer.analyze(parseResults);
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

    private void forceGC() {
        Object obj = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get() != null) {
            System.gc();
        }
    }
}
