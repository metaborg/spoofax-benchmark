package org.metaborg.spoofax.benchmark.cmd;

import java.io.File;
import java.util.Collection;

import org.metaborg.spoofax.benchmark.cmd.commands.CollectCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.CommonArguments;
import org.metaborg.spoofax.benchmark.cmd.commands.ExportHistoryCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.ExportSingleCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.ProcessCommand;
import org.metaborg.spoofax.benchmark.core.Facade;
import org.metaborg.spoofax.benchmark.core.collect.CollectedData;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;

public class Main {
	public static void main(String[] args) {
		final CommonArguments common = new CommonArguments();
		final JCommander jc = new JCommander(common);

		final CollectCommand cmdSerialize = new CollectCommand();
		jc.addCommand(CollectCommand.NAME, cmdSerialize);
		final ProcessCommand cmdProcess = new ProcessCommand();
		jc.addCommand(ProcessCommand.NAME, cmdProcess);
		final ExportSingleCommand cmdSingleExport = new ExportSingleCommand();
		jc.addCommand(ExportSingleCommand.NAME, cmdSingleExport);
		final ExportHistoryCommand cmdHistoryExport = new ExportHistoryCommand();
		jc.addCommand(ExportHistoryCommand.NAME, cmdHistoryExport);

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
				case ExportSingleCommand.NAME: {
					final ProcessedData data = facade.deserializeProcessed(new File(cmdSingleExport.inputFile));
					switch(cmdSingleExport.outputFormat) {
						case "csv": {
							facade.exportSingleCSV(data, new File(cmdSingleExport.outputDirectory));
							break;
						}
						case "image": {
							facade.exportSingleImage(data, new File(cmdSingleExport.outputDirectory));
							break;
						}
					}

					break;
				}
				case ExportHistoryCommand.NAME: {
					final Collection<ProcessedData> historicalData = Lists.newLinkedList();
					for(String file : cmdHistoryExport.inputFiles) {
						historicalData.add(facade.deserializeProcessed(new File(file)));
					}
					switch(cmdHistoryExport.outputFormat) {
						case "image": {
							facade.exportHistoryImage(historicalData, new File(cmdHistoryExport.outputDirectory));
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
