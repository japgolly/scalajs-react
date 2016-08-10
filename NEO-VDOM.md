NEO VDOM
========

* Deprecate the `bool ?= whatever` syntax

* Ensure operator consistency thoughout.

* Ensure composition consistency thoughout.
  (i.e. remove `TagMod.{compose,+}`)

* Clarify all operators. Update doc.

* WTF is with the extra tags/attr in `Extra.scala`??!
  Move that shit into where it belongs.


ReactAttr
=========

* Attributes have types.

* Attribute types will be applied gradually. (i.e. ½ will be Any to at first)

* Callback attributes should know their event types!
  Won't know the underlying dom type though.

* Boolean tags will be like `^.x := b`. Also allow `{^.x, !^.x, ^.x.not}`?

```scala
Attr := Value

Attr :=? Option[Value]

// Conditional syntax
// Parens are annoying but it's best for readability.
// Inefficiency negligable so long as := is light.
(Attr := Value) when   bool
(Attr := Value) unless bool

// Failed conditional syntax
Attr := Value when bool   // doesn't work due to hardcoded Scala op precedence
Attr := when(bool, Value) // namespace problem. "when" is too generic.
when(bool)(Attr := Value) // namespace problem. "when" is too generic.
(bool) ?= (Attr := Value) // Yuk.
Attr := Value /= bool     // Too cryptic (= denotes when)
Attr := Value /! bool     // Too cryptic (! denotes unless)
Attr := Value %= bool     // Too cryptic (= denotes when)
Attr := Value %! bool     // Too cryptic (! denotes unless)

// No Attr := SeqLike[Value] syntax
// Not used; or if is, use typeclass and existing syntax.

// Callbacks
Attr --> Callback
Attr ==> Callback.Fn // Change this?
Attr(e => Callback) // ?
// TBD
```


ReactStyle
==========

Follow whatever `ReactAttr` does.


Tags
====

Scope:
* Frag
* ReactTag
* ReactElement
* TagMod

* Remove SeqLike[TagLike] stuff.

* Prevent subtags in void-tags?

Refs
====

TODO


Breaking Changes
================

Should really add these back in and deprecate them

* Use `^.dangerouslySetInnerHtml := x` instead of `^.dangerouslySetInnerHtml(x)`.
* Import `vdom.html_<^._` instead of `vdom.prefix_<^._`.
* `?=` deprecated in favour of `when`/`unless`.
* Change `:=` to `:=?` when the right-hand side is an `Option`.
* TagMod: `+` and `compose` replaced with `apply(TagMod*)` just like tags.
* Use `ReactAttr[A](x)` in place of `"".reactAttr`.
* Use `ReactAttr.style[A](x)` in place of `"".reactStyle`.
* No more auto conversion of vdom arrays. Either use `blah: _*`, `TagMod(blah: _*)`, or `blah.toReactArray`, `ReactArray(…)`.


# DON'T FORGET!! Go through all the TODOs in `vdom`

