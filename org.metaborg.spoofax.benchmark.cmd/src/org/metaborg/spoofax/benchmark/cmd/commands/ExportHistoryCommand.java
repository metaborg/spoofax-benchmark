package org.metaborg.spoofax.benchmark.cmd.commands;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;

@Parameters(commandDescription = "Exports multiple processed data files into historical statistics.", separators = "=")
public class ExportHistoryCommand {
	public static final String NAME = "export-history";


	@Parameter(names = { "--infiles", "-i" }, required = true, variableArity = true,
		description = "Files containing the processed data.")
	public final List<String> inputFiles = Lists.newLinkedList();


	@Parameter(names = { "--outdir", "-o" }, required = true,
		description = "Directory where the exporter should output files.")
	public String outputDirectory;

	@Parameter(names = { "--outfmt", "-f" }, required = true,
		description = "Format of the exporter, choose from: image.")
	public String outputFormat;
}
