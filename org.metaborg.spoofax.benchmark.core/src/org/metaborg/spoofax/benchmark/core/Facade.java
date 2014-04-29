package org.metaborg.spoofax.benchmark.core;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.benchmark.core.collect.DataCollector;
import org.metaborg.spoofax.benchmark.core.collect.DataSerializer;
import org.metaborg.spoofax.benchmark.core.collect.RawData;
import org.metaborg.spoofax.benchmark.core.export.CSVExporter;
import org.metaborg.spoofax.benchmark.core.export.ImageExporter;
import org.metaborg.spoofax.benchmark.core.process.DataProcessor;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

public final class Facade {
	private final IOAgent agent;
	private final ITermFactory termFactory;


	public Facade() {
		this.termFactory = new ImploderOriginTermFactory(new TermFactory());
		this.agent = new IOAgent();
	}


	public RawData collect(String languageDir, String languageName, String projectDir) {
		final DataCollector collector = new DataCollector(languageDir, languageName, projectDir, agent, termFactory);
		return collector.collect();
	}

	public void serialize(RawData data, File serializeDirectory) throws IOException {
		final DataSerializer serializer = new DataSerializer(termFactory, agent);
		serializer.serialize(data, serializeDirectory);
	}

	public void
		collectAndSerialize(String languageDir, String languageName, String projectDir, File serializeDirectory)
			throws IOException {
		serialize(collect(languageDir, languageName, projectDir), serializeDirectory);
	}

	public RawData deserialize(File serializedDirectory) {
		final DataSerializer serializer = new DataSerializer(termFactory, agent);
		return serializer.deserialize(serializedDirectory);
	}


	public ProcessedData process(RawData data) {
		final DataProcessor processor = new DataProcessor();
		return processor.process(data);
	}

	public ProcessedData process(String languageDir, String languageName, String projectDir) {
		return process(collect(languageDir, languageName, projectDir));
	}

	public ProcessedData processFromSerialized(File serializedDirectory) {
		return process(deserialize(serializedDirectory));
	}


	public void exportCSV(ProcessedData data, File exportDirectory) throws IllegalArgumentException,
		IllegalAccessException, IOException {
		final CSVExporter exporter = new CSVExporter();
		exporter.export(data, exportDirectory);
	}

	public void exportImage(ProcessedData data, File exportDirectory) throws IOException {
		final ImageExporter exporter = new ImageExporter();
		exporter.export(data, exportDirectory);
	}
}
