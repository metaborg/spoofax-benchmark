package org.metaborg.spoofax.benchmark.cmd;

import java.io.File;

import org.metaborg.spoofax.benchmark.cmd.commands.CollectCommand;
import org.metaborg.spoofax.benchmark.cmd.commands.CommonArguments;
import org.metaborg.spoofax.benchmark.cmd.commands.ExportCSVCommand;
import org.metaborg.spoofax.benchmark.core.Facade;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
	public static void main(String[] args) {
		final CommonArguments common = new CommonArguments();
		final JCommander jc = new JCommander(common);

		final CollectCommand cmdSerialize = new CollectCommand();
		jc.addCommand(CollectCommand.NAME, cmdSerialize);
		final ExportCSVCommand cmdExportCSV = new ExportCSVCommand();
		jc.addCommand(ExportCSVCommand.NAME, cmdExportCSV);

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

			switch (jc.getParsedCommand()) {
				case CollectCommand.NAME: {
					facade.collectAndSerialize(cmdSerialize.languageDirectory, cmdSerialize.languageName,
						cmdSerialize.projectDirectory, new File(cmdSerialize.outputDirectory));
					break;
				}
				case ExportCSVCommand.NAME: {
					final String error = cmdExportCSV.validate();
					if(error != null) {
						error(jc, error);
						return;
					}

					if(cmdExportCSV.deserializationRequired()) {
						facade.exportCSVFromSerialized(new File(cmdExportCSV.inputDirectory), new File(
							cmdExportCSV.outputDirectory));
					} else if(cmdExportCSV.collectRequired()) {
						facade.exportCSV(cmdExportCSV.languageDirectory, cmdExportCSV.languageName,
							cmdExportCSV.projectDirectory, new File(cmdExportCSV.outputDirectory));
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
