package org.metaborg.spoofax.benchmark.cmd.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Exports collected data to CSV format.", separators = "=")
public class ExportCSVCommand {
	public static final String NAME = "export-csv";

	@Parameter(names = { "--langdir", "-l" })
	public String languageDirectory;

	@Parameter(names = { "--langname", "-n" })
	public String languageName;

	@Parameter(names = { "--projdir", "-p" })
	public String projectDirectory;


	@Parameter(names = { "--inputdir", "-i" })
	public String inputDirectory;


	@Parameter(names = { "--outputdir", "-o" }, required = true)
	public String outputDirectory;


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
