#!/bin/bash

set -euo pipefail
cd "$(dirname "$0")/library"

io=../index.html
ii=ghpages/html/prod.html
js=ghpages/res/ghpages.js
files="$io $js $js.map"

rm -f $files

perl -pe '
    s!"res/!"library/ghpages/res/!g;
    s!"target/.+?\.js"!"'"$js"'"!g;
  ' > $io < $ii

sbt ghpages/fullOptJS

url=https://raw.githubusercontent.com/japgolly/scalajs-react/$(git rev-parse HEAD)/
perl -pi -e 's|\Qfile://'"$(pwd)"'/\E|'"$url"'|g' $js.map

git add $files
git st
echo "cd library && git commit -m 'Refresh ghpages' -- $files"
echo
