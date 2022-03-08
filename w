#!/bin/bash
set -euo pipefail

ls -1 \
  $HOME/.ivy2/local/org.scala-js/sbt-scalajs/scala_2.12/sbt_1.0/1.9.1-SNAPSHOT/ivys/ivy.xml \
  ./testReactRefresh/*/src/*/scala/japgolly/scalajs/react/test/reactrefresh/*.scala \
  ./project/*.s* \
  | entr -crs "sbt trrc 2>&1 | tee $HOME/projects/forks/scala-js/output.txt"
