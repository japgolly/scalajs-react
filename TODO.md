# React 19 TODO (1/2)

- [x] Create UMD build of R19
- [x] Upgrade UMD build to 19.2.4

- [x] Make library tests use R19: tests

- [x] Delete deprecated code
  - [x] Delete code deprecated in 1.x
  - [x] Delete code deprecated in 2.x
  - [x] Delete code deprecated in 3.x
  - [x] Review code deprecated in ""
- [x] Delete facadeTest

- [x] tests-dep: Either
  - [x] use R19, or
  - [x] delete (and delete deprecated main code too)

- [x] Make downstream tests use R19: js
- [x] Make downstream tests use R19: jsCE
- [x] Make downstream tests use R19: jsCBIO

- [x] Make gh-pages use R19

- [x] Update documentation for R19

# React 19 TODO (2/2)

- [ ] Support new R19 changes and features
  - [ ] React 19.0
    - [x] Add `onCaughtError` and `onUncaughtError` options to `{create,hydrate}Root`
    - [x] `useTransition.startTransition` now accepts async
    - [x] New hook: useActionState
    - [x] `action` and `formAction` props of `<form>`, `<input>`, and `<button>` elements
    - [x] new `requestFormReset` React DOM API
    - [x] React DOM: New hook: `useFormStatus`
    - [x] New hook: `useOptimistic`
    - [x] New API: `use`
    - [x] You can render <Context> as a provider instead of <Context.Provider>`
    - [x] Cleanup functions for refs
    - [x] `useDeferredValue` initial value
    - [x] `precedence` attr to `link`s
    - [x] `import { prefetchDNS, preconnect, preload, preinit } from 'react-dom'`
  - [x] React 19.1
  - [ ] React 19.2
    - [ ] `<Activity>`
    - [ ] `useEffectEvent`
