# 0.8.3 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.8.2...v0.8.3))

* Added Touch event attributes:
    * `onTouchCancel`
    * `onTouchEnd`
    * `onTouchMove`
    * `onTouchStart`
* Added example for Touch events. ([Live demo](http://japgolly.github.io/scalajs-react/))
* Added new styles:
    * `alignSelf`
    * `alignContent`

# 0.8.2 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.8.1...v0.8.2))

* Upgrade to Scala.JS 0.6.1.
* Upgrade dependencies to versions built with Scala.JS 0.6.1.
* Hide internal methods.


# 0.8.1 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.8.0...v0.8.1))

* Added `ExternalVar` to `extra` module. ([Live demo](http://japgolly.github.io/scalajs-react/))
* Added `ReactComponentB.static` to quickly create components with unchanging content.
* Added new styles:
    * `alignItems`
    * `flex`
    * `flexBasis`
    * `flexDirection`
    * `flexGrow`
    * `flexShrink`
    * `flexWrap`
    * `justifyContent`
    * `vectorEffect`
* Fixed typos in `borderRightStyle` and `lineHeight`.
* Fixed warnings in `ReactCssTransitionGroup`. ([#86](https://github.com/japgolly/scalajs-react/issues/86))


# 0.8.0 ([commit log](https://github.com/japgolly/scalajs-react/compare/v0.7.2...v0.8.0))

##### Breaking
* Upgrade [scalajs-dom](https://github.com/scala-js/scala-js-dom) from 0.7.0 to 0.8.0.
  Note the [package reorg](https://github.com/scala-js/scala-js-dom/commit/8208d792ad0a32dce7b4b9ea53f0d27040a7a7f3).
* Removed deprecated code scheduled for deletion in 0.8.0.

##### Scalatags
* Fixed React warnings about certain styles.
* Added `spreadMethod` attribute.
* Added `aria` attributes.

##### Other
* `.isMounted()` now accessible from all component scope views.
