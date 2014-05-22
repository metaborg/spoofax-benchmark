#!/bin/bash

LANG_LOC="$1"
LANG_NAME="$2"
PROJ_LOC="$3"

java -jar spxbench.jar \
  collect \
    "--langdir=$LANG_LOC" \
    "--langname=$LANG_NAME" \
    "--projdir=$PROJ_LOC" \
    "--outdir=$PROJ_LOC/collect" \
    --warmups=10 \
    --measurements=30 \
"|||" \
  process \
    "--indir=$PROJ_LOC/collect" \
    "--outfile=$PROJ_LOC/processed/processed.dat" \
    --noindex \
    --notaskengine \
"|||" \
  export-single \
    "--infile=$PROJ_LOC/processed/processed.dat" \
    "--outdir=$PROJ_LOC/benchmark" \
    --outfmt=image \
