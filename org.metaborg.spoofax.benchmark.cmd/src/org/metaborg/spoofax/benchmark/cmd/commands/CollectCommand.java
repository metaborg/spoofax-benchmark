package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Collects data and serializes that data to disk.", separators = "=")
public class CollectCommand {
	public static final String NAME = "collect";

	@Parameter(
		names = { "--langdir", "-l" },
		required = true,
		description = "Directory where the language to collect benchmark results for can be found. Incompatible with --inputdir.")
	public String languageDirectory;

	@Parameter(names = { "--langname", "-n" }, required = true,
		description = "Name of the language to collect benchmark results for. Incompatible with --inputdir.")
	public String languageName;

	@Parameter(names = { "--projdir", "-p" }, required = true,
		description = "Directory of the project to analyze using given language. Incompatible with --inputdir.")
	public String projectDirectory;


	@Parameter(names = { "--outdir", "-o" }, required = true,
		description = "Directory where the collected results should be stored.")
	public String outputDirectory;
}
