#!/bin/bash
cd "$(dirname "$0")" || exit 1

ii=gh-pages/index.html
io=index.html

rm -f $io

cat $ii | perl -pe '
    s!"res/!"gh-pages/res/!g;
    s!"target/.+?\.js"!"gh-pages/res/ghpages.js"!g;
    ' > $io \
  && sbt gh-pages/fullOptJS

