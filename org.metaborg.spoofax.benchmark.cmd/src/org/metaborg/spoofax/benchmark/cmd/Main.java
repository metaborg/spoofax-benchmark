package org.metaborg.spoofax.benchmark.cmd;

import java.io.File;

import org.metaborg.spoofax.benchmark.cmd.commands.CollectCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.CommonArguments;
import org.metaborg.spoofax.benchmark.cmd.commands.ExportCommand;
import org.metaborg.spoofax.benchmark.core.Facade;
import org.metaborg.spoofax.benchmark.core.process.ProcessedData;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
	public static void main(String[] args) {
		final CommonArguments common = new CommonArguments();
		final JCommander jc = new JCommander(common);

		final CollectCommand cmdSerialize = new CollectCommand();
		jc.addCommand(CollectCommand.NAME, cmdSerialize);
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
					facade.collectAndSerialize(cmdSerialize.languageDirectory, cmdSerialize.languageName,
						cmdSerialize.projectDirectory, new File(cmdSerialize.outputDirectory));
					break;
				}
				case ExportCommand.NAME: {
					final String error = cmdExport.validate();
					if(error != null) {
						error(jc, error);
						return;
					}

					final ProcessedData data;
					if(cmdExport.deserializationRequired()) {
						data = facade.processFromSerialized(new File(cmdExport.inputDirectory));
					} else {
						data =
							facade.process(cmdExport.languageDirectory, cmdExport.languageName,
								cmdExport.projectDirectory);
					}

					switch(cmdExport.outputFormat) {
						case "print": {
							break;
						}
						case "csv": {
							facade.exportCSV(data, new File(cmdExport.outputDirectory));
							break;
						}
						case "image": {
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
