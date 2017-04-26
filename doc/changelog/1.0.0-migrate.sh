#!/bin/bash

find . -name '*.scala' -type f -exec perl -pi -e '
  undef $/;
  use Regexp::Common;
  $x= "(){"."}";
  $bp = "(?:$RE{balanced}{-parens=>$x})";
  $arg = "(?:[^()}{,]*$bp?)+";

  s/(import +[a-z.]*)ReactComponentB(?![A-Za-z])/\1ScalaComponent/g;
  s/ReactComponentB *(?=\[)/ScalaComponent.build/g;
  s/ReactComponentB\.static/ScalaComponent.static/g;
  s/(?<!\w)FunctionalComponent(?!\w)/ScalaFnComponent/g;
  s/(?<!\w)CallbackB(?!\w)/CallbackTo[Boolean]/g;
  s/((?:componentDid|componentWill|shouldComponent)[a-zA-Z]+)CB/\1Const/g;
  s/\. *\$ *\. *(?=backend)/./g;
  s/\[ *P, *S, *B, *N *\<: *TopNode *\]( *=\s*)\( *_ *: *ScalaComponent.build\[ *P, *S, *B, *N *\] *\)\s+/[P, C <: Children, S, B]: ScalaComponentConfig[P, C, S, B]\1_/;
  s/_((?:set|mod|run)State)/\1Fn/g;
  s/(forceUpdate|getDOMNode|isMounted)\(\)/\1/g;
  s/ReactDOM *\. *render *\(($arg) *, *($arg) *\)/\1.renderIntoDOM(\3)/g;
  s/React *\. *addons/ReactAddons/g;

  s/prefix_\<\^/html_<^/g;
  s/(?<!\w)React(Tag|TagOf|Attr|Node|Element)(?!\w)/Vdom\1/g;
  s/(?<!\w)EmptyTag(?!\w)/EmptyVdom/g;
  s/("\S*?") *\. *react(Attr|Style)/Vdom\2(\1)/g;
  s/(disabled +:= +)"disabled"/\1true/g;
  s/toReactNodeArray/toVdomArray/g;
  s/dangerouslySetInnerHtml *\( *($arg) *\)/dangerouslySetInnerHtml := \1/g;

  s/(React(?:[A-Z][a-z]+)?Event)H/\1FromHtml/g;
  s/(React(?:[A-Z][a-z]+)?Event)I/\1FromInput/g;
  s/(React(?:[A-Z][a-z]+)?Event)TA/\1FromTextArea/g;
  s/Synthetic((?:[A-Z][a-z]+|UI)?Event)/React\1From/g;

  s/ReusableFn *($bp) *\. *(set|mod)State/ReusableFn.state\1.\3/g;
  s/(?<!\w)ReusableFn(?!\w)/Reusable.fn/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ExternalVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *\. *zoomL *\( *([A-Za-z][A-Za-z. ]*?) *\) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/ReusableVar *\. *state *\( *([a-zA-Z0-9\$]+) *zoomL *([A-Za-z][A-Za-z. ]*?) *\)/StateSnapshot.withReuse.zoomL(\2).of(\1)/g;
  s/([:\[] *)(?:External|Reusable)Var *(?=\[)/\1StateSnapshot/g;
  s/(?<!\w)ReusableVar( +(?=[a-z])| *[(.])/StateSnapshot.withReuse\1/g;
  s/(?<!\w)ExternalVar( +(?=[a-z])| *[(.])/StateSnapshot\1/g;
  s/(?<!\w)(import +[^\n]*)(?:External|Reusable)Var(?!\w)/\1StateSnapshot/g;
  s/(?<!\w)ReusableVal2?(?!\w)/Reusable\1/g;

  s/(?<=Listenable\.)installS/listenWithStateMonad/g;
  s/(?<=Listenable\.)installU/listenToUnit/g;
  s/(?<=Listenable\.)install(?![a-zA-Z])/listen/g;
  s/(Reusability *\.? *when)True/\1/g;
  s/(Reusability *)\. *fn(?=[\(\[])/\1/g;

  s/Px($bp) *(?=[.\n])/Px\1.withReuse.manualUpdate/g;
  s/Px *\. *const($bp) *(?=[.\n])/Px.constByValue\1/g;
  s/Px *\. *lazyConst($bp) *(?=[.\n])/Px.constByNeed\1/g;
  s/Px *\. *thunkM($bp) *(?=[.\n])/Px\1.withReuse.manualRefresh/g;
  s/Px *\. *thunkA($bp) *(?=[.\n])/Px\1.withReuse.autoRefresh/g;
  s/Px *\. *cbM($bp) *(?=[.\n])/Px.callback\1.withReuse.manualRefresh/g;
  s/Px *\. *cbA($bp) *(?=[.\n])/Px.callback\1.withReuse.autoRefresh/g;
  s/Px *\. *bs(\([^\n;]+\)) *\. *propsM($bp) *(?=[.\n])/Px.props\1.map\2.withReuse.manualRefresh/g;
  s/Px *\. *bs(\([^\n;]+\)) *\. *propsA($bp) *(?=[.\n])/Px.props\1.map\2.withReuse.autoRefresh/g;
  s/Px *\. *bs(\([^\n;]+\)) *\. *stateM($bp) *(?=[.\n])/Px.state\1.map\2.withReuse.manualRefresh/g;
  s/Px *\. *bs(\([^\n;]+\)) *\. *stateA($bp) *(?=[.\n])/Px.state\1.map\2.withReuse.autoRefresh/g;
  s/Px *\. *bs($bp) *\. *propsM *(?=[.\n])/Px.props\1.withReuse.manualRefresh/g;
  s/Px *\. *bs($bp) *\. *propsA *(?=[.\n])/Px.props\1.withReuse.autoRefresh/g;
  s/Px *\. *bs($bp) *\. *stateM *(?=[.\n])/Px.state\1.withReuse.manualRefresh/g;
  s/Px *\. *bs($bp) *\. *stateA *(?=[.\n])/Px.state\1.withReuse.autoRefresh/g;
  s/Px *\. *NoReuse($bp) *(?=[.\n])/Px\1.withoutReuse.manualUpdate/g;
  s/Px *\. *NoReuse *\. *thunkM($bp) *(?=[.\n])/Px\1.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *thunkA($bp) *(?=[.\n])/Px\1.withoutReuse.autoRefresh/g;
  s/Px *\. *NoReuse *\. *cbM($bp) *(?=[.\n])/Px.callback\1.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *cbA($bp) *(?=[.\n])/Px.callback\1.withoutReuse.autoRefresh/g;
  s/Px *\. *NoReuse *\. *bs(\([^\n;]+\)) *\. *propsM($bp) *(?=[.\n])/Px.props\1.map\2.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *bs(\([^\n;]+\)) *\. *propsA($bp) *(?=[.\n])/Px.props\1.map\2.withoutReuse.autoRefresh/g;
  s/Px *\. *NoReuse *\. *bs(\([^\n;]+\)) *\. *stateM($bp) *(?=[.\n])/Px.state\1.map\2.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *bs(\([^\n;]+\)) *\. *stateA($bp) *(?=[.\n])/Px.state\1.map\2.withoutReuse.autoRefresh/g;
  s/Px *\. *NoReuse *\. *bs($bp) *\. *propsM *(?=[.\n])/Px.props\1.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *bs($bp) *\. *propsA *(?=[.\n])/Px.props\1.withoutReuse.autoRefresh/g;
  s/Px *\. *NoReuse *\. *bs($bp) *\. *stateM *(?=[.\n])/Px.state\1.withoutReuse.manualRefresh/g;
  s/Px *\. *NoReuse *\. *bs($bp) *\. *stateA *(?=[.\n])/Px.state\1.withoutReuse.autoRefresh/g;

  s/(Change|Keyboard|Mouse)EventData/SimEvent.\1/g;
  s/ReactTestUtils *\. *(?=Simulate)//g;
  s/(?<=\n) *import +Simulate *\n//g;
  s/(?<=\n) *import +japgolly.scalajs.react.test.Simulate *\n+( *import +japgolly.scalajs.react.test._ *\n)/\1/g;
  s/(?<=\n)( *import +japgolly.scalajs.react.test._ *\n)\s*import +japgolly.scalajs.react.test.Simulate *\n/\1/g;
  s/(?<=remove|ithout)ReactDataAttr/ReactInternals/g;

  s/(\$[ .]+)zoomL(?!\w)/\1zoomStateL/g;

  # RC1 -> RC2
  s/(ScalaComponent\s*\.\s*build)(\s*\[)/\1er\2/g;
  s/(ScalaComponent\s*\.\s*static\s*\(\s*".*?")\s*, */\1)(/;

  # RC2 -> RC3
  s/initialStateCB_P/initialStateCallbackFromProps/g;
  s/initialStateCB/initialStateCallback/g;
  s/initialState_P/initialStateFromProps/g;
  s/ScalaComponentConfig/ScalaComponent.Config/g;
  s/(Callback(?:To|Option)? *(?:\. *)?(?:traverse|sequence))O(?!\w)/\1Option/g;
' {} +
