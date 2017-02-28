#!/bin/bash

find . -name '*.scala' -type f -exec perl -pi -e '
  undef $/;
  s/(import +[a-z.]*)ReactComponentB(?![A-Za-z])/\1ScalaComponent/g;
  s/ReactComponentB *(?=\[)/ScalaComponent.build/g;
  s/ReactComponentB\.static/ScalaComponent.static/g;
  s/(?<!\w)FunctionalComponent(?!\w)/ScalaFnComponent/g;
  s/((?:componentDid|componentWill|shouldComponent)[a-zA-Z]+)CB/\1Const/g;
  s/\. *\$ *\. *(?=backend)/./g;
  s/\[ *P, *S, *B, *N *\<: *TopNode *\]( *=\s*)\( *_ *: *ScalaComponent.build\[ *P, *S, *B, *N *\] *\)\s+/[P, C <: Children, S, B]: ScalaComponentConfig[P, C, S, B]\1_/;
  s/_((?:set|mod|run)State)/\1Fn/g;
  s/(getDOMNode|isMounted)\(\)/\1/g;

  s/prefix_\<\^/html_<^/g;
  s/(?<!\w)React(Tag|TagOf|Attr|Node|Element)(?!\w)/Vdom\1/g;
  s/(?<!\w)EmptyTag(?!\w)/EmptyVdom/g;
  s/("\S*?") *\. *react(Attr|Style)/Vdom\2(\1)/g;
  s/(disabled +:= +)"disabled"/\1true/g;
  s/toReactNodeArray/toVdomArray/g;

  s/(React(?:[A-Z][a-z]+)?Event)H/\1FromHtml/g;
  s/(React(?:[A-Z][a-z]+)?Event)I/\1FromInput/g;
  s/(React(?:[A-Z][a-z]+)?Event)TA/\1FromTextArea/g;
  s/Synthetic((?:[A-Z][a-z]+)?Event)/React\1From/g;

  s/ReusableFn *\(([^()]+?)\) *\. *(set|mod)State/ReusableFn.state(\1).\2/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/([:\[] *)(?:External|Reusable)Var *(?=\[)/\1StateSnapshot/g;
  s/(?<!\w)ReusableVar( +(?=[a-z])| *[(.])/StateSnapshot.withReuse\1/g;
  s/(?<!\w)ExternalVar( +(?=[a-z])| *[(.])/StateSnapshot\1/g;
  s/(?<!\w)(import +[^\n]*)(?:External|Reusable)Var(?!\w)/\1StateSnapshot/g;

  s/(?<=Listenable\.)installS/listenWithStateMonad/g;
  s/(?<=Listenable\.)installU/listenToUnit/g;
  s/(?<=Listenable\.)install(?![a-zA-Z])/listen/g;
  s/(Reusability *\.? *when)True/\1/g;
  s/(Reusability *)\. *fn(?=[\(\[])/\1/g;

  s/(Change|Keyboard|Mouse)EventData/SimEvent.\1/g;
  s/ReactTestUtils *\. *(?=Simulate)//g;
  s/(?<=\n) *import +Simulate *\n//g;
  s/(?<=\n) *import +japgolly.scalajs.react.test.Simulate *\n+( *import +japgolly.scalajs.react.test._ *\n)/\1/g;
  s/(?<=\n)( *import +japgolly.scalajs.react.test._ *\n)\s*import +japgolly.scalajs.react.test.Simulate *\n/\1/g;

  s/(\$[ .]+)zoomL(?!\w)/\1zoomStateL/g;

' {} +
  # s/(\. *component[a-zA-Z]+ *\( *_ *\.) *\$ *\. *(?=backend)/\1/g;
