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
	
	@Parameter(names = { "--notime", "-nt" }, description = "Don't process time data.")
	public boolean dontProcessTimeData;
	@Parameter(names = { "--noindex", "-ni" }, description = "Don't process index data.")
	public boolean dontProcessIndexData;
	@Parameter(names = { "--notaskengine", "-nte" }, description = "Don't process task engine data.")
	public boolean dontProcessTaskEngineData;
}
