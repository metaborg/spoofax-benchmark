package org.metaborg.spoofax.benchmark.core;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.benchmark.core.collect.CollectedData;
import org.metaborg.spoofax.benchmark.core.collect.CollectedDataSerializer;
import org.metaborg.spoofax.benchmark.core.collect.DataCollector;
import org.metaborg.spoofax.benchmark.core.export.history.ImageHistoryExporter;
import org.metaborg.spoofax.benchmark.core.export.single.CSVSingleExporter;
import org.metaborg.spoofax.benchmark.core.export.single.ImageSingleExporter;
import org.metaborg.spoofax.benchmark.core.process.DataProcessor;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;
import org.metaborg.spoofax.benchmark.core.process.ProcessedDataSerializer;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

public final class Facade {
    private final IOAgent agent;
    private final ITermFactory termFactory;

    private final CollectedDataSerializer collectedSerializer;
    private final ProcessedDataSerializer processedSerializer;

    private final DataProcessor processor = new DataProcessor();

    private final CSVSingleExporter csvSingleExporter = new CSVSingleExporter();
    private final ImageSingleExporter imageSingleExporter = new ImageSingleExporter();

    private final ImageHistoryExporter imageHistoryExporter = new ImageHistoryExporter();


    public Facade() {
        this.agent = new IOAgent();
        this.termFactory = new ImploderOriginTermFactory(new TermFactory());
        this.collectedSerializer = new CollectedDataSerializer(termFactory);
        this.processedSerializer = new ProcessedDataSerializer();
    }


    public CollectedData collect(String languageDir, String languageName, String projectDir, int warmupPhases,
        int measurementPhases) throws IOException, ParseException, AnalysisException {
        final DataCollector collector = new DataCollector(languageDir, languageName, projectDir, agent, termFactory);
        return collector.collect(warmupPhases, measurementPhases);
    }

    public void serializeCollected(CollectedData data, File serializeDirectory) throws IOException {
        collectedSerializer.serialize(data, serializeDirectory);
    }

    public void collectAndSerialize(String languageDir, String languageName, String projectDir, int warmupPhases,
        int measurementPhases, File serializeDirectory) throws IOException, ParseException, AnalysisException {
        serializeCollected(collect(languageDir, languageName, projectDir, warmupPhases, measurementPhases),
            serializeDirectory);
    }

    public CollectedData deserializeCollected(File serializedDirectory) throws Exception {
        return collectedSerializer.deserialize(serializedDirectory);
    }


    public ProcessedData process(CollectedData collectedData, boolean processTimeData, boolean processIndexData,
        boolean processTaskEngineData) {
        return processor.process(collectedData, processTimeData, processIndexData, processTaskEngineData);
    }

    public void serializeProcessed(ProcessedData data, File serializeFilename) throws IOException {
        processedSerializer.serialize(data, serializeFilename);
    }

    public ProcessedData processAndSerialize(CollectedData data, boolean processTimeData, boolean processIndexData,
        boolean processTaskEngineData, File serializeFilename) throws IOException {
        final ProcessedData processedData =
            processor.process(data, processTimeData, processIndexData, processTaskEngineData);
        serializeProcessed(processedData, serializeFilename);
        return processedData;
    }

    public ProcessedData processFromSerializedCollected(File serializedDirectory, boolean processTimeData,
        boolean processIndexData, boolean processTaskEngineData) throws Exception {
        return process(deserializeCollected(serializedDirectory), processTimeData, processIndexData,
            processTaskEngineData);
    }

    public ProcessedData deserializeProcessed(File serializeFilename) throws Exception {
        return processedSerializer.deserialize(serializeFilename);
    }


    public void exportSingleCSV(ProcessedData data, File exportDirectory) throws Exception {
        csvSingleExporter.export(data, exportDirectory);
    }

    public void exportSingleImage(ProcessedData data, File exportDirectory) throws IOException {
        imageSingleExporter.export(data, exportDirectory);
    }


    public void exportHistoryImage(Iterable<ProcessedData> historicalData, File exportDirectory) throws IOException {
        imageHistoryExporter.export(historicalData, exportDirectory);
    }
}
