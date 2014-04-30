package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Exports processed data to another format.", separators = "=")
public class ExportCommand {
	public static final String NAME = "export";


	@Parameter(names = { "--infile", "-i" }, required = true, description = "File containing the processed data.")
	public String inputFile;


	@Parameter(names = { "--outdir", "-o" }, required = true,
		description = "Directory where the exporter should output files.")
	public String outputDirectory;

	@Parameter(names = { "--outfmt", "-f" }, required = true,
		description = "Format of the exporter, choose from: csv, image.")
	public String outputFormat;
}
