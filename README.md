Spoofax benchmarker (spxbench)
=================

Tools for benchmarking Spoofax projects.

# Usage

First download the latest JAR of spxbench from the releases section: [https://github.com/metaborg/spoofax-benchmark/releases]().

Spoofax benchmarker is a command line tool, to get help on its commands and options, just run the jar file with java using the `-h` option:


    java -jar spxbench.jar -h
  
  
which will output the command line usage information:
    
    
	Usage: <main class> [options] [command] [command options]
	  Options:
	    --help, -h
	       Shows usage help
	       Default: false
	  Commands:
	    collect      Collects data and serializes it to a directory.
	      Usage: collect [options]
	        Options:
	        * --langdir, -l
	             Directory where the language to collect benchmark results for can
	             be found.
	        * --langname, -n
	             Name of the language to collect benchmark results for.
	          --measurements, -m
	             Number of measurement phases to perform. Time data is averaged over
	             all measurement phases.
	             Default: 1
	        * --outdir, -o
	             Directory where the collected results should be stored.
	        * --projdir, -p
	             Directory of the project to analyze using given language.
	          --warmups, -w
	             Number of warmup phases to perform before performing measurements.
	             Default: 0

	    process      Processes collected data and serializes it to a file.
	      Usage: process [options]
	        Options:
	        * --indir, -i
	             Directory where the collected results can be found.
	          --noindex, -ni
	             Don't process index data.
	             Default: false
	          --notaskengine, -nte
	             Don't process task engine data.
	             Default: false
	          --notime, -nt
	             Don't process time data.
	             Default: false
	        * --outfile, -o
	             File where the processed data should be stored.

	    export-single      Exports a single processed data file into detailed statistics.
	      Usage: export-single [options]
	        Options:
	        * --infile, -i
	             File containing the processed data.
	        * --outdir, -o
	             Directory where the exporter should output files.
	        * --outfmt, -f
	             Format of the exporter, choose from: csv, image.

	    export-history      Exports multiple processed data files into historical statistics.
	      Usage: export-history [options]
	        Options:
	        * --infiles, -i
	             Files containing the processed data.
	             Default: []
	        * --outdir, -o
	             Directory where the exporter should output files.
	        * --outfmt, -f
	             Format of the exporter, choose from: image.


Commands are executed by using the command name as the first argument, followed by any options of that command. For example, to execute the `collect` command, use:


    java -jar spxbench.jar collect --langdir=./java-front --langname=Java \
		--projdir=./java-examples --outdir./benchmark/collect
    
    
# Benchmarking a Spoofax project

Benchmarking consists of collecting data, processing that data, and then exporting the processed data into a readable format, such as a report or images. To do this all in one go, invoke the following:

    java -jar spxbench.jar \
      collect \
        --langdir=$LANG_LOC \
        --langname=$LANG_NAME \
        --projdir=$PROJ_LOC \
        --outdir=$PROJ_LOC/collect \
        --warmups=10 \
        --measurements=30 \
    "|||" \
      process \
        --indir=$PROJ_LOC/collect \
        --outfile=$PROJ_LOC/processed/processed.dat \
        --noindex \
        --notaskengine \
    "|||" \
      export-single \
        --infile=$PROJ_LOC/processed/processed.dat \
        --outdir=$PROJ_LOC/benchmark \
        --outfmt=image

where

* `$LANG_LOC` is the location of the Spoofax project of the language you are benchmarking.
* `$LANG_NAME` is the name of that language.
* `$PROJ_LOC` is the location of the project that contains the test files.

Spoofax benchmarker will:

1. load the language at the given location
2. analyze all files at the project location
3. collect raw data during analysis
4. store the collected raw data in `$PROJ_LOC/collect`
5. process the collected data and store it in `$PROJ_LOC/processed/processed.dat`
6. export the processed data to images in `$PROJ_LOC/benchmark`

