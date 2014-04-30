package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(commandDescription = "Collects data and serializes it to a directory.", separators = "=")
public class CollectCommand {
	public static final String NAME = "collect";

	
	@ParametersDelegate
	public final CollectArguments collectArguments = new CollectArguments();


	@Parameter(names = { "--outdir", "-o" }, required = true,
		description = "Directory where the collected results should be stored.")
	public String outputDirectory;
}
