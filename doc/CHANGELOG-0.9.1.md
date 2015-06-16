# 0.9.1

* Fixed `onDblClick` so that it uses `onDoubleClick` as React expects, and
  added a `onDoubleClick` alias.
* Upgrade [scala-js-dom](https://github.com/scala-js/scala-js-dom) to 0.8.1.
* Add `uuid` to the Router2 route building DSL.
* New `ReactEventTA` aliases for TextArea events.
* Add nice `.toString` methods to `Px` classes.
* Add `ReusableFn.renderComponent` which recreates a `Props ~=> ReactElement`.
* Add `ReactComponentB.configureSpec` - useful for JS interop.
* Add `ReusableVal2[A, S]` which is a lazy value `A` whose reusability is determined by a source value `S`.
