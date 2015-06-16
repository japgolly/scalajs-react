# 0.9.1

* Fixed `onDblClick` so that it uses `onDoubleClick` as React expects, and
  added a `onDoubleClick` alias.
* Upgrade [scala-js-dom](https://github.com/scala-js/scala-js-dom) to 0.8.1.
* Add `uuid` to the Router2 route buiding DSL.
* New `ReactEventTA` aliases for TextArea events.
* Add nice `.toString` methods to `Px` classes.
* Add `ReusableFn.renderComponent` which recreates a `Props ~=> ReactElement`.
* Add `ReusableVal.renderComponent` which recreates a `ReusableVal[(Props, ReactElement)]`.
* Add `ReusableVal.function` which takes an `A => B` and a reusable `A` to create a `ReusableVal[(A, B)]`.
* Add `ReactComponentB.configureSpec` - useful for JS interop.
