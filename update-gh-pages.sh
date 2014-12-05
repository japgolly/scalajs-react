#!/bin/bash
cd "$(dirname "$0")" || exit 1

ii=gh-pages/index.html
io=index.html
js=gh-pages/res/ghpages.js

rm -f $io $js

cat $ii | perl -pe '
    s!"res/!"gh-pages/res/!g;
    s!"target/.+?\.js"!"'"$js"'"!g;
    ' > $io \
  && sbt gh-pages/fullOptJS \
  && git add $js $io \
  && git st \
  && echo "git commit -m 'Refresh gh-pages'" && echo

