package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class CommonArguments {
	@Parameter(names = { "--help", "-h" }, description = "Shows usage help", help = true)
	public boolean help;
}
