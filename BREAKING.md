### VDOM

* `^.dangerouslySetInnerHtml := x` instead of `^.dangerouslySetInnerHtml(x)`.
* Import `vdom.html_<^._` instead of `vdom.prefix_<^._`.
* `?=` deprecated in favour of `when`/`unless`.
* Change `:=` to `:=?` when the right-hand side is an `Option`.
* TagMod: `+` and `compose` replaced with `apply(TagMod*)` just like tags.
* Use `ReactAttr[A]("")` in place of `"".reactAttr`.
* Use `ReactAttr.style[A]("")` in place of `"".reactStyle`.
* Use `HtmlTag("")` or `HtmlTagOf[N]("")` in place of `"".reactTag`.
* No more auto conversion of vdom arrays. Either use `blah: _*`, `TagMod(blah: _*)`, or `blah.toReactArray`, `ReactArray(…)`.

