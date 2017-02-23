#!/bin/bash

find . -name '*.scala' -type f -exec perl -pi -e '
  undef $/;
  s/(import +[a-z.]*)ReactComponentB(?![A-Za-z])/\1ScalaComponent/g;
  s/ReactComponentB *(?=\[)/ScalaComponent.build/g;
  s/ReactComponentB\.static/ScalaComponent.static/g;
  s/prefix_\<\^/html_<^/g;
  s/((?:componentDid|componentWill|shouldComponent)[a-zA-Z]+)CB/\1Const/g;
  s/(?<=Listenable\.)installS/listenWithStateMonad/g;
  s/(?<=Listenable\.)installU/listenToUnit/g;
  s/(?<=Listenable\.)install(?![a-zA-Z])/listen/g;
  s/(React(?:[A-Z][a-z]+)?Event)H/\1FromHtml/g;
  s/(React(?:[A-Z][a-z]+)?Event)I/\1FromInput/g;
  s/(React(?:[A-Z][a-z]+)?Event)TA/\1FromTextArea/g;
  s/Synthetic((?:[A-Z][a-z]+)?Event)/React\1From/g;
  s/\[ *P, *S, *B, *N *\<: *TopNode *\]( *=\s*)\( *_ *: *ScalaComponent.build\[ *P, *S, *B, *N *\] *\)\s+/[P, C <: Children, S, B]: ScalaComponentConfig[P, C, S, B]\1_/;
  s/_((?:set|mod|run)State)/\1Fn/g;
  s/ReusableFn *\(([^()]+?)\) *\. *(set|mod)State/ReusableFn.state(\1).\2/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/ExternalVar|ReusableVar/StateSnapshot/g;
' {} +
