#!/bin/bash
cd "$(dirname "$(readlink -e "$0")")/.." || exit 1

[ $# -ne 1 ] && echo "Usage: $0 <version>" && exit 1
ver="$1"

perl -pi -e 's/(?<![a-zA-Z0-9_-])[1-3]\d\.\d+\.\d+(?![a-zA-Z0-9_-])/'"$ver"'/g' \
      {doc,project}/*.* build.sbt *.md gh-pages/*.html \
  && git diff -U0

