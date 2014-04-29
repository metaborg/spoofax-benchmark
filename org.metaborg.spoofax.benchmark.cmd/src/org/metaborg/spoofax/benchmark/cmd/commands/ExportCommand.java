package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Exports collected data to another format.", separators = "=")
public class ExportCommand {
	public static final String NAME = "export";

	@Parameter(
		names = { "--langdir", "-l" },
		description = "Directory where the language to collect benchmark results for can be found. Incompatible with --inputdir.")
	public String languageDirectory;

	@Parameter(names = { "--langname", "-n" },
		description = "Name of the language to collect benchmark results for. Incompatible with --inputdir.")
	public String languageName;

	@Parameter(names = { "--projdir", "-p" },
		description = "Directory of the project to analyze using given language. Incompatible with --inputdir.")
	public String projectDirectory;


	@Parameter(
		names = { "--indir", "-i" },
		description = "Directory where collected results can be found. Incompatible with --langdir, --langname, and --projdir.")
	public String inputDirectory;


	@Parameter(names = { "--outdir", "-o" }, required = true,
		description = "Directory where the exporter should output files.")
	public String outputDirectory;

	@Parameter(names = { "--outfmt", "-f" }, required = true,
		description = "Format of the exporter, choose from: csv, image.")
	public String outputFormat;


	public boolean collectRequired() {
		return languageDirectory != null || languageName != null || projectDirectory != null;
	}

	public boolean deserializationRequired() {
		return inputDirectory != null;
	}


	public String validate() {
		if(collectRequired() && deserializationRequired())
			return "Input directory (--inputdir) option cannot be used in conjunction with language and project (--langdir, --langname, --projdir) options.";
		if(!collectRequired() && !deserializationRequired())
			return "Choose either an input directory (--inputdir), or provide language and project (--langdir, --langname, --projdir) options.";
		if(collectRequired() && (languageDirectory == null || languageName == null || projectDirectory == null))
			return "If any language or project (--langdir, --langname, --projdir) option is used, all three must be specified.";

		return null;
	}
}
