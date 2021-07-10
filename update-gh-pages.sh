#!/bin/bash

set -euo pipefail
cd "$(dirname "$0")"

ii=ghpages/html/prod.html
io=index.html
js=ghpages/res/ghpages.js

rm -f $io $js

perl -pe '
    s!"res/!"ghpages/res/!g;
    s!"target/.+?\.js"!"'"$js"'"!g;
  ' > $io < $ii

sbt ghpages/fullOptJS
git add $js $io
git st
echo "git commit -m 'Refresh ghpages'"
echo
