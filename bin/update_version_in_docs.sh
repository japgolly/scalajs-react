#!/bin/bash
cd "$(dirname "$(readlink -e "$0")")/.." || exit 1

[ $# -ne 1 ] && echo "Usage: $0 <version>" && exit 1
ver="$1"

find . -name '*.md' -exec perl -pi -e 's/(japgolly.scalajs-react.+)"0(?:\.\d+){2}"/\1"'"$ver"'"/' {} + \
  && perl -pi -e 's/(?<="'"$ver"')-SNAPSHOT//' build.sbt \
  && perl -pi -e 's/(Latest.+\/changelog\/)[0-9.]+?(?=\.md)/${1}'"$ver"'/' README.md \
  && perl -pi -e 's!(javadoc\.io\S*?/)[0-9.]+(?=\))!${1}'"$ver"'!g' README.md \
  && git diff

