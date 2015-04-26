# 0.9.0

* Performance Management
  * Detail with examples here: [PERF.md](https://github.com/japgolly/scalajs-react/blob/master/extra/PERF.md)
  * New API: `Addons.Perf` over http://facebook.github.io/react/docs/perf.html.
  * `Reusable` for fast & safe `shouldComponentUpdate` impl.
  * `ReusableFn` for callbacks that can be passed around and work with `shouldComponentUpdate`.
  * `ReusableVar` as a version of `ExternalVar` that works with `shouldComponentUpdate`.
  * `Px` for caching.
