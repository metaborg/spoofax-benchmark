package org.metaborg.spoofax.benchmark.cmd;

import java.io.File;
import java.util.Arrays;
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
	public static void main(String[] multipleArgs) {
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

		for(String[] args : splitMultipleCommands(multipleArgs)) {

			try {
				jc.parse(args);
			} catch(ParameterException e) {
				error(jc, e, true);
				return;
			}

			if(common.help) {
				error(jc, null, true);
				return;
			}

			try {
				final Facade facade = new Facade();

				switch(jc.getParsedCommand()) {
					case CollectCommand.NAME: {
						facade.collectAndSerialize(cmdSerialize.collectArguments.languageDirectory,
							cmdSerialize.collectArguments.languageName, cmdSerialize.collectArguments.projectDirectory,
							cmdSerialize.collectArguments.warmupPhases,
							cmdSerialize.collectArguments.measurementPhases, new File(cmdSerialize.outputDirectory));
						break;
					}
					case ProcessCommand.NAME: {
						final CollectedData data = facade.deserializeCollected(new File(cmdProcess.inputDirectory));
						facade.processAndSerialize(data, !cmdProcess.dontProcessTimeData,
							!cmdProcess.dontProcessIndexData, !cmdProcess.dontProcessTaskEngineData, new File(
								cmdProcess.outputFile));
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
				error(jc, e, false);
				return;
			}
		}
	}

	private static void error(JCommander jc, Exception exception, boolean showUsage) {
		if(exception != null) {
			exception.printStackTrace(System.err);
		}
		if(showUsage) {
			jc.usage();
		}
		System.exit(1);
	}

	private static Iterable<String[]> splitMultipleCommands(String[] args) {
		final Collection<String[]> commands = Lists.newArrayList();
		int start = 0;
		for(int i = 0; i < args.length; ++i) {
			final String arg = args[i];
			if(arg.equals("|||")) {
				commands.add(Arrays.copyOfRange(args, start, i));
				start = i + 1;
			}
		}
		if(start <= args.length) {
			commands.add(Arrays.copyOfRange(args, start, args.length));
		}
		return commands;
	}
}
