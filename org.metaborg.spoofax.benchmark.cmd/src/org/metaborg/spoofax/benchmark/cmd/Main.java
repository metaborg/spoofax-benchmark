package org.metaborg.spoofax.benchmark.cmd;

import java.io.File;

import org.metaborg.spoofax.benchmark.cmd.commands.CollectCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.CommonArguments;
import org.metaborg.spoofax.benchmark.cmd.commands.ExportCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.ProcessCommand;
import org.metaborg.spoofax.benchmark.core.Facade;
import org.metaborg.spoofax.benchmark.core.collect.CollectedData;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
	public static void main(String[] args) {
		final CommonArguments common = new CommonArguments();
		final JCommander jc = new JCommander(common);

		final CollectCommand cmdSerialize = new CollectCommand();
		jc.addCommand(CollectCommand.NAME, cmdSerialize);
		final ProcessCommand cmdProcess = new ProcessCommand();
		jc.addCommand(ProcessCommand.NAME, cmdProcess);
		final ExportCommand cmdExport = new ExportCommand();
		jc.addCommand(ExportCommand.NAME, cmdExport);

		try {
			jc.parse(args);
		} catch(ParameterException e) {
			error(jc, e.getMessage());
			return;
		}

		if(common.help) {
			error(jc, null);
			return;
		}

		try {
			final Facade facade = new Facade();

			switch(jc.getParsedCommand()) {
				case CollectCommand.NAME: {
					facade.collectAndSerialize(cmdSerialize.collectArguments.languageDirectory,
						cmdSerialize.collectArguments.languageName, cmdSerialize.collectArguments.projectDirectory,
						cmdSerialize.collectArguments.warmupPhases, new File(cmdSerialize.outputDirectory));
					break;
				}
				case ProcessCommand.NAME: {
					final CollectedData data = facade.deserializeCollected(new File(cmdProcess.inputDirectory));
					facade.processAndSerialize(data, new File(cmdProcess.outputFile));
					break;
				}
				case ExportCommand.NAME: {
					final ProcessedData data = facade.deserializeProcessed(new File(cmdExport.inputFile));
					switch(cmdExport.outputFormat) {
						case "csv": {
							facade.exportCSV(data, new File(cmdExport.outputDirectory));
							break;
						}
						case "image": {
							facade.exportImage(data, new File(cmdExport.outputDirectory));
							break;
						}
					}

					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void error(JCommander jc, String message) {
		if(message != null)
			System.out.println(message);
		jc.usage();
	}
}
