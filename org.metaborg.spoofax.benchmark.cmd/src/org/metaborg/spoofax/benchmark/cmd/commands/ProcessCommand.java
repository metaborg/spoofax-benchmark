package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Processes collected data and serializes it to a file.", separators = "=")
public class ProcessCommand {
	public static final String NAME = "process";

	
	@Parameter(names = { "--indir", "-i" }, description = "Directory where the collected results can be found.",
		required = true)
	public String inputDirectory;


	@Parameter(names = { "--outfile", "-o" }, description = "File where the processed data should be stored.",
		required = true)
	public String outputFile;
}
