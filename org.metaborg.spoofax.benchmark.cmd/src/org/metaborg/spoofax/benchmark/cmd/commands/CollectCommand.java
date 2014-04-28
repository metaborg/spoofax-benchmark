package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Collects data and serializes that data to disk.", separators = "=")
public class CollectCommand {
	public static final String NAME = "collect";

	@Parameter(names = { "--langdir", "-l" }, required = true)
	public String languageDirectory;

	@Parameter(names = { "--langname", "-n" }, required = true)
	public String languageName;

	@Parameter(names = { "--projdir", "-p" }, required = true)
	public String projectDirectory;

	
	@Parameter(names = { "--outputdir", "-o" }, required = true)
	public String outputDirectory;
}
