package org.metaborg.spoofax.benchmark.core;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.benchmark.core.collect.CollectedData;
import org.metaborg.spoofax.benchmark.core.collect.CollectedDataSerializer;
import org.metaborg.spoofax.benchmark.core.collect.DataCollector;
import org.metaborg.spoofax.benchmark.core.export.CSVExporter;
import org.metaborg.spoofax.benchmark.core.export.ImageExporter;
import org.metaborg.spoofax.benchmark.core.process.DataProcessor;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;
import org.metaborg.spoofax.benchmark.core.process.ProcessedDataSerializer;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

public final class Facade {
	private final IOAgent agent;
	private final ITermFactory termFactory;
	private final CollectedDataSerializer collectedSerializer;
	private final DataProcessor processor = new DataProcessor();
	private final ProcessedDataSerializer processedSerializer = new ProcessedDataSerializer();
	private final CSVExporter csvExporter = new CSVExporter();
	private final ImageExporter imageExporter = new ImageExporter();


	public Facade() {
		this.termFactory = new ImploderOriginTermFactory(new TermFactory());
		this.agent = new IOAgent();
		this.collectedSerializer = new CollectedDataSerializer(termFactory, agent);
	}


	public CollectedData collect(String languageDir, String languageName, String projectDir, int warmupPhases) {
		final DataCollector collector = new DataCollector(languageDir, languageName, projectDir, agent, termFactory);
		return collector.collect(warmupPhases);
	}

	public void serializeCollected(CollectedData data, File serializeDirectory) throws IOException {
		collectedSerializer.serialize(data, serializeDirectory);
	}

	public void collectAndSerialize(String languageDir, String languageName, String projectDir, int warmupPhases,
		File serializeDirectory) throws IOException {
		serializeCollected(collect(languageDir, languageName, projectDir, warmupPhases), serializeDirectory);
	}

	public CollectedData deserializeCollected(File serializedDirectory) throws Exception {
		return collectedSerializer.deserialize(serializedDirectory);
	}


	public ProcessedData process(CollectedData collectedData) {
		return processor.process(collectedData);
	}

	public void serializeProcessed(ProcessedData data, File serializeFilename) throws IOException {
		processedSerializer.serialize(data, serializeFilename);
	}

	public ProcessedData processAndSerialize(CollectedData data, File serializeFilename) throws IOException {
		final ProcessedData processedData = processor.process(data);
		serializeProcessed(processedData, serializeFilename);
		return processedData;
	}

	public ProcessedData processFromSerializedCollected(File serializedDirectory) throws Exception {
		return process(deserializeCollected(serializedDirectory));
	}

	public ProcessedData deserializeProcessed(File serializeFilename) throws Exception {
		return processedSerializer.deserialize(serializeFilename);
	}


	public void exportCSV(ProcessedData data, File exportDirectory) throws Exception {
		csvExporter.export(data, exportDirectory);
	}

	public void exportImage(ProcessedData data, File exportDirectory) throws IOException {
		imageExporter.export(data, exportDirectory);
	}
}
