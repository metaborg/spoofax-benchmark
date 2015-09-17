package org.metaborg.spoofax.benchmark.core.collect;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.ContextIdentifier;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.LanguageFileSelector;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.runtime.task.engine.TaskManager;
import org.metaborg.spoofax.core.analysis.taskengine.AnalysisTimeResult;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzerData;
import org.metaborg.spoofax.core.context.ISpoofaxContext;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.attachments.TermAttachmentStripper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.internal.Lists;
import com.google.inject.TypeLiteral;

public final class DataCollector {
    private final String languageDir;
    private final String languageName;
    private final String projectDir;
    private final ITermFactory termFactory;

    public DataCollector(String languageDir, String languageName, String projectDir, ITermFactory termFactory) {
        this.languageDir = languageDir;
        this.languageName = languageName;
        this.projectDir = projectDir;
        this.termFactory = termFactory;

        // Setup sunshine
        org.metaborg.sunshine.drivers.Main.jc = new JCommander();
        final String[] sunshineArgs =
            new String[] { "--project", projectDir, "--auto-lang", languageDir, "--observer", "analysis-default-cmd",
                "--non-incremental" };
        final SunshineMainArguments params = new SunshineMainArguments();
        final boolean argsFine = org.metaborg.sunshine.drivers.Main.parseArguments(sunshineArgs, params);
        if(!argsFine) {
            System.exit(1);
        }
        params.validate();
        org.metaborg.sunshine.drivers.Main.initEnvironment(params);
    }

    public CollectedData collect(int warmupPhases, int measurementPhases) throws ParseException, IOException,
        AnalysisException {
        final ServiceRegistry services = ServiceRegistry.INSTANCE();
        final IResourceService resources = services.getService(IResourceService.class);
        final ILanguageService languages = services.getService(ILanguageService.class);
        final ILanguageIdentifierService identifier = services.getService(ILanguageIdentifierService.class);
        final ISourceTextService sourceText = services.getService(ISourceTextService.class);
        final ISyntaxService<IStrategoTerm> parser =
            services.getService(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {});
        final IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer =
            services.getService(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {});

        final ILanguage language = languages.getLanguage(languageName);
        final ILanguageImpl languageImpl = language.activeImpl();
        final FileObject projectLoc = resources.resolve(projectDir);
        final FileObject[] files = projectLoc.findFiles(new LanguageFileSelector(identifier, languageImpl));

        final IContext context =
            ServiceRegistry.INSTANCE().getService(IContextFactory.class)
                .create(new ContextIdentifier(projectLoc, languageImpl));

        for(int i = 0; i < warmupPhases; ++i) {
            analyze(sourceText, parser, analyzer, languageImpl, context, files);
        }

        final List<AnalysisResult<IStrategoTerm, IStrategoTerm>> allResults = Lists.newLinkedList();
        for(int i = 0; i < measurementPhases; ++i) {
            final AnalysisResult<IStrategoTerm, IStrategoTerm> result =
                analyze(sourceText, parser, analyzer, languageImpl, context, files);
            allResults.add(result);
        }
        if(allResults.size() == 0)
            throw new RuntimeException("Could not analyze files.");

        final AnalysisResult<IStrategoTerm, IStrategoTerm> firstResult = allResults.get(0);

        final CollectedData data = new CollectedData();
        data.languageDirectory = languageDir;
        data.languageName = languageName;
        data.projectDirectory = projectDir;

        final TermAttachmentStripper attachmentStripper = new TermAttachmentStripper(termFactory);
        for(final AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : firstResult.fileResults) {
            final FileData fileData = new FileData();
            fileData.name = fileResult.source.getName().getPath();
            fileData.ast = attachmentStripper.strip(fileResult.result);
            for(IMessage message : fileResult.messages) {
                fileData.messages.add(message.message());
            }
            data.files.add(fileData);
        }

        final ISpoofaxContext spoofaxContext = (ISpoofaxContext) context;
        data.indexFile = IndexManager.cacheFile(context.location());
        data.index = spoofaxContext.index();

        data.taskEngineFile = TaskManager.cacheFile(context.location());
        data.taskEngine = spoofaxContext.taskEngine();

        final TaskEngineAnalyzerData firstAnalyzerData = (TaskEngineAnalyzerData) firstResult.analyzerData;
        data.debug = firstAnalyzerData.debugResult;

        for(final AnalysisResult<IStrategoTerm, IStrategoTerm> result : allResults) {
            final TaskEngineAnalyzerData analyzerData = (TaskEngineAnalyzerData) result.analyzerData;
            final AnalysisTimeResult timeResult = analyzerData.timeResult;
            data.time.add("Parse", timeResult.parse);
            data.time.add("Pre-trans", timeResult.preTrans);
            data.time.add("Collect", timeResult.collect);
            data.time.add("Task eval", timeResult.taskEval);
            data.time.add("Post-trans", timeResult.postTrans);
            data.time.add("Index persist", timeResult.indexPersist);
            data.time.add("Task engine persist", timeResult.taskPersist);
        }

        return data;
    }

    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(ISourceTextService sourceText,
        ISyntaxService<IStrategoTerm> parser, IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer,
        ILanguageImpl language, IContext context, FileObject[] files) throws ParseException, IOException,
        AnalysisException {
        context.reset();
        forceGC();

        final Collection<ParseResult<IStrategoTerm>> parseResults = Lists.newLinkedList();
        for(FileObject file : files) {
            parseResults.add(parser.parse(sourceText.text(file), file, language, null));
        }

        return analyzer.analyze(parseResults, context);
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
