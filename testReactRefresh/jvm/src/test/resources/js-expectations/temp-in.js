'use strict';

import * as $CtorSummoner                                 from "./japgolly.scalajs.react.CtorType$Summoner$.js";
import * as $Reusable                                     from "./japgolly.scalajs.react.Reusable$.js";
import * as $CallbackTo                                   from "./japgolly.scalajs.react.callback.CallbackTo.js";
import * as $HooksApiPrimary                              from "./japgolly.scalajs.react.hooks.Api$Primary.js";
import * as $HooksApiSecondaryWithRender                  from "./japgolly.scalajs.react.hooks.Api$SecondaryWithRender.js";
import * as $HooksComponentP_SubsequentStepsanon1         from "./japgolly.scalajs.react.hooks.ComponentP_SubsequentSteps$$anon$1.js";
import * as $HookComponentBuilder                         from "./japgolly.scalajs.react.hooks.HookComponentBuilder$.js";
import * as $HookComponentBuilderComponentPFirstStep      from "./japgolly.scalajs.react.hooks.HookComponentBuilder$ComponentP$FirstStep.js";
import * as $HookComponentBuilderComponentPSubsequentStep from "./japgolly.scalajs.react.hooks.HookComponentBuilder$ComponentP$SubsequentStep$.js";
import * as $HooksUseStateF                               from "./japgolly.scalajs.react.hooks.Hooks$UseStateF.js";
import * as $Singleton                                    from "./japgolly.scalajs.react.internal.Singleton$.js";
import * as $react_package                                from "./japgolly.scalajs.react.package$.js";
import * as $VdomAttrEventCallback                        from "./japgolly.scalajs.react.vdom.Attr$EventCallback$.js";
import * as $VdomExports                                  from "./japgolly.scalajs.react.vdom.Exports$.js";
import * as $VdomHtmlTagOf                                from "./japgolly.scalajs.react.vdom.HtmlTagOf$.js";
import * as $VdomNode                                     from "./japgolly.scalajs.react.vdom.VdomNode$.js";
import * as $VdomNodeanon1                                from "./japgolly.scalajs.react.vdom.VdomNode$$anon$1.js";
import * as $Vdomhtml_lessup                              from "./japgolly.scalajs.react.vdom.html_$less$up$.js";
import * as $Object                                       from "./java.lang.Object.js";
import * as $Function1                                    from "./scala.Function1.js";
import * as $AnonFunction0                                from "./scala.scalajs.runtime.AnonFunction0.js";
import * as $AnonFunction2                                from "./scala.scalajs.runtime.AnonFunction2.js";
import * as $WrappedVarArgs                               from "./scala.scalajs.runtime.WrappedVarArgs.js";

/*
function $Counter$() {
  this.Ljapgolly_scalajs_react_test_reactrefresh_Counter$__f_Component = null;
  $n_Ljapgolly_scalajs_react_test_reactrefresh_Counter$ = this;
  $react_package.$m_Ljapgolly_scalajs_react_package$();

  var this$4       = $HookComponentBuilder.$m_Ljapgolly_scalajs_HookComponentBuilder$().apply__Ljapgolly_scalajs_HookComponentBuilder$ComponentP$First();
  var initialState = new $AnonFunction0.$c_sjsr_AnonFunction0((this$2 => () => 0)(this));
  var step         = new $HookComponentBuilderComponentPFirstStep.$c_Ljapgolly_scalajs_HookComponentBuilder$ComponentP$FirstStep();
  var this$18      = $HooksApiSecondaryWithRender.$as_Ljapgolly_scalajs_HooksApi$SecondaryWithRender($HooksApiPrimary.$f_Ljapgolly_scalajs_HooksApi$Primary__useState__F0__Ljapgolly_scalajs_HooksApi$AbstractStep__O(this$4, initialState, step));

  var f = new $AnonFunction2.$c_sjsr_AnonFunction2((this$2$1 => (x$1$2, s$2) => {
    $Object.$as_jl_Void(x$1$2);
    var s = $HooksUseStateF.$as_Ljapgolly_scalajs_Hooks$UseStateF(s$2);
    var $$x2 = $VdomHtmlTagOf.$m_Ljapgolly_scalajs_VdomHtmlTagOf$();
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    $VdomExports.$m_Ljapgolly_scalajs_VdomExports$();
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    $VdomNode.$m_Ljapgolly_scalajs_VdomNode$();
    var $$x1 = new $VdomNodeanon1.$c_Ljapgolly_scalajs_VdomNode$$anon$1("Count is ");
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    var v = $Object.$uI(s.Ljapgolly_scalajs_Hooks$UseStateF$$anon$2__f_raw[0]);
    $VdomNode.$m_Ljapgolly_scalajs_VdomNode$();
    var array = [$$x1, new $VdomNodeanon1.$c_Ljapgolly_scalajs_VdomNode$$anon$1(v), $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$().Ljapgolly_scalajs_Vdomhtml_$less$up$__f_$up.Ljapgolly_scalajs_VdomHtmlAttrAndStyles$__f_onClick.$minus$minus$greater__F0__F1__Ljapgolly_scalajs_VdomTagMod(new $AnonFunction0.$c_sjsr_AnonFunction0(((this$11, s$1) => () => {
      $Reusable.$m_Ljapgolly_scalajs_Reusable$();
      var r = $HooksUseStateF.$f_Ljapgolly_scalajs_Hooks$UseStateF__modState__Ljapgolly_scalajs_Reusable(s$1);
      return new $CallbackTo.$c_Ljapgolly_scalajs_CallbackTo($CallbackTo.$as_Ljapgolly_scalajs_CallbackTo($Function1.$as_F1(r.value__O()).apply__O__O(new $Function1.$c_sjsr_AnonFunction1((this$13 => x$2$2 => {
        var x$2 = $Object.$uI(x$2$2);
        return 1 + x$2 | 0;
      })(this$11)))).Ljapgolly_scalajs_CallbackTo__f_japgolly$scalajs$react$callback$CallbackTo$$trampoline);
    })(this$2$1, s)), $VdomAttrEventCallback.$m_Ljapgolly_scalajs_VdomAttr$EventCallback$().Ljapgolly_scalajs_VdomAttr$EventCallback$__f_defaultSync)];
    return $$x2.apply$extension__T__sci_Seq__Ljapgolly_scalajs_VdomTagOf("button", new $WrappedVarArgs.$c_sjsr_WrappedVarArgs(array));
  })(this));

  var this$17 = $HookComponentBuilderComponentPSubsequentStep.$m_Ljapgolly_scalajs_HookComponentBuilder$ComponentP$SubsequentStep$();
  var step$1  = new $HooksComponentP_SubsequentStepsanon1.$c_Ljapgolly_scalajs_HooksComponentP_SubsequentSteps$$anon$1(this$17);
  var s$3     = $CtorSummoner.$m_Ljapgolly_scalajs_react_CtorType$Summoner$().summonN__Ljapgolly_scalajs_Singleton__Ljapgolly_scalajs_react_CtorType$Summoner($Singleton.$m_Ljapgolly_scalajs_Singleton$().Ljapgolly_scalajs_Singleton$__f_BoxUnit);

  this.Ljapgolly_scalajs_react_test_reactrefresh_Counter$__f_Component = $HooksApiSecondaryWithRender.$f_Ljapgolly_scalajs_HooksApi$SecondaryWithRender__render__O__Ljapgolly_scalajs_HooksApi$SubsequentStep__Ljapgolly_scalajs_react_CtorType$Summoner__Ljapgolly_scalajs_react_component_JsBaseComponentTemplate$ComponentWithRoot(this$18, f, step$1, s$3);
}
*/

import React from "React"; // <-------------------------------

function $Counter$() {
  $react_package.$m_Ljapgolly_scalajs_react_package$();

  var this$4       = $HookComponentBuilder.$m_Ljapgolly_scalajs_HookComponentBuilder$().apply__Ljapgolly_scalajs_HookComponentBuilder$ComponentP$First();
  var initialState = new $AnonFunction0.$c_sjsr_AnonFunction0((this$2 => () => 0)(this));
  var step         = new $HookComponentBuilderComponentPFirstStep.$c_Ljapgolly_scalajs_HookComponentBuilder$ComponentP$FirstStep();
  var this$18      = $HooksApiSecondaryWithRender.$as_Ljapgolly_scalajs_HooksApi$SecondaryWithRender($HooksApiPrimary.$f_Ljapgolly_scalajs_HooksApi$Primary__useState__F0__Ljapgolly_scalajs_HooksApi$AbstractStep__O(this$4, initialState, step));

  var f = new $AnonFunction2.$c_sjsr_AnonFunction2((this$2$1 => (x$1$2, s$2) => {
    const temp = React.useState(0) // <-------------------------------

    $Object.$as_jl_Void(x$1$2);
    var s = $HooksUseStateF.$as_Ljapgolly_scalajs_Hooks$UseStateF(s$2);
    var $$x2 = $VdomHtmlTagOf.$m_Ljapgolly_scalajs_VdomHtmlTagOf$();
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    $VdomExports.$m_Ljapgolly_scalajs_VdomExports$();
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    $VdomNode.$m_Ljapgolly_scalajs_VdomNode$();
    var $$x1 = new $VdomNodeanon1.$c_Ljapgolly_scalajs_VdomNode$$anon$1("Count is ");
    $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$();
    var v = $Object.$uI(s.Ljapgolly_scalajs_Hooks$UseStateF$$anon$2__f_raw[0]);
    $VdomNode.$m_Ljapgolly_scalajs_VdomNode$();
    var array = [$$x1, new $VdomNodeanon1.$c_Ljapgolly_scalajs_VdomNode$$anon$1(v), $Vdomhtml_lessup.$m_Ljapgolly_scalajs_Vdomhtml_$less$up$().Ljapgolly_scalajs_Vdomhtml_$less$up$__f_$up.Ljapgolly_scalajs_VdomHtmlAttrAndStyles$__f_onClick.$minus$minus$greater__F0__F1__Ljapgolly_scalajs_VdomTagMod(new $AnonFunction0.$c_sjsr_AnonFunction0(((this$11, s$1) => () => {
      $Reusable.$m_Ljapgolly_scalajs_Reusable$();
      var r = $HooksUseStateF.$f_Ljapgolly_scalajs_Hooks$UseStateF__modState__Ljapgolly_scalajs_Reusable(s$1);
      return new $CallbackTo.$c_Ljapgolly_scalajs_CallbackTo($CallbackTo.$as_Ljapgolly_scalajs_CallbackTo($Function1.$as_F1(r.value__O()).apply__O__O(new $Function1.$c_sjsr_AnonFunction1((this$13 => x$2$2 => {
        var x$2 = $Object.$uI(x$2$2);
        return 1 + x$2 | 0;
      })(this$11)))).Ljapgolly_scalajs_CallbackTo__f_japgolly$scalajs$react$callback$CallbackTo$$trampoline);
    })(this$2$1, s)), $VdomAttrEventCallback.$m_Ljapgolly_scalajs_VdomAttr$EventCallback$().Ljapgolly_scalajs_VdomAttr$EventCallback$__f_defaultSync)];
    return $$x2.apply$extension__T__sci_Seq__Ljapgolly_scalajs_VdomTagOf("button", new $WrappedVarArgs.$c_sjsr_WrappedVarArgs(array));
  })(this));

  var this$17 = $HookComponentBuilderComponentPSubsequentStep.$m_Ljapgolly_scalajs_HookComponentBuilder$ComponentP$SubsequentStep$();
  var step$1  = new $HooksComponentP_SubsequentStepsanon1.$c_Ljapgolly_scalajs_HooksComponentP_SubsequentSteps$$anon$1(this$17);
  var s$3     = $CtorSummoner.$m_Ljapgolly_scalajs_react_CtorType$Summoner$().summonN__Ljapgolly_scalajs_Singleton__Ljapgolly_scalajs_react_CtorType$Summoner($Singleton.$m_Ljapgolly_scalajs_Singleton$().Ljapgolly_scalajs_Singleton$__f_BoxUnit);

  return $HooksApiSecondaryWithRender.$f_Ljapgolly_scalajs_HooksApi$SecondaryWithRender__render__O__Ljapgolly_scalajs_HooksApi$SubsequentStep__Ljapgolly_scalajs_react_CtorType$Summoner__Ljapgolly_scalajs_react_component_JsBaseComponentTemplate$ComponentWithRoot(this$18, f, step$1, s$3);
}

export default $Counter$
