#!/bin/bash
cd "$(dirname "$(readlink -e "$0")")/.." || exit 1

[ $# -ne 1 ] && echo "Usage: $0 <version>" && exit 1
ver="$1"
verpat='(?:[01](?:\.\d+)+(?:-[a-zA-Z0-9.-]+)?)'

find . -name '*.md' -exec perl -pi -e 's/(japgolly.scalajs-react.+)"'"$verpat"'"/\1"'"$ver"'"/' {} + \
  && perl -pi -e 's/(?<="'"$ver"')-SNAPSHOT//' build.sbt \
  && perl -pi -e '
       s/(Latest.+\/changelog\/)'"$verpat"'(?=\.md)/${1}'"$ver"'/;
       s/(?<=\*)(v?)'"$verpat"'(?=\*)/${1}'"$ver"'/;
       s!(javadoc\.io\S*?/)'"$verpat"'(?=\))!${1}'"$ver"'!g;
     ' README.md \
  && git diff

