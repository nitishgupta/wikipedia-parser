#!/bin/bash

DIRS=mentions/*
for d in $DIRS
do
	FILES=$d/*
	FNAME=merged_mentions/$(basename $d.mens)
	cat $FILES > $FNAME
	shuf $FNAME -o $FNAME.shuf
	mv $FNAME.shuf $FNAME
	echo "$FNAME"
done
