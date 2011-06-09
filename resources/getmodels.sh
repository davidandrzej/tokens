#!/bin/bash

# Stoplist from: 
# 
# David D. Lewis, Yiming Yang, Tony G. Rose, and Fan Li. 2004. RCV1: A
# New Benchmark Collection for Text Categorization
# Research. J. Mach. Learn. Res. 5 (December 2004), 361-397.
#

MODELDIR=./

URLS=(
    'http://opennlp.sourceforge.net/models-1.5/en-token.bin'
    'http://jmlr.csail.mit.edu/papers/volume5/lewis04a/a11-smart-stop-list/english.stop'
)

for URL in ${URLS[@]}
do
    wget $URL --directory-prefix=$MODELDIR
done

