package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class CollectArguments {
	@Parameter(names = { "--langdir", "-l" }, required = true,
		description = "Directory where the language to collect benchmark results for can be found.")
	public String languageDirectory;

	@Parameter(names = { "--langname", "-n" }, required = true,
		description = "Name of the language to collect benchmark results for.")
	public String languageName;

	@Parameter(names = { "--projdir", "-p" }, required = true,
		description = "Directory of the project to analyze using given language.")
	public String projectDirectory;


	@Parameter(names = { "--warmups", "-w" }, description = "Number of warmup phases to perform before performing measurements.")
	public int warmupPhases = 0;
	@Parameter(names = { "--measurements", "-m" }, description = "Number of measurement phases to perform. Time data is averaged over all measurement phases.")
	public int measurementPhases = 1;
}
