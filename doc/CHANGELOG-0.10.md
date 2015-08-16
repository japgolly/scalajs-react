# 0.10.0 (unreleased)

* Replaced Router v1 with v2.

  `extra.router` has been removed; `extra.router2` has been renamed to take its place.

  If for some reason, you want to use Router v1, don't want to migrate to v2 *and* want to keep up-to-date with scalajs-react,
  please copy Router v1 into your own codebase.
  Maintaining two Routers in scalajs-react is not good.

<br>
Migration commands:
```sh
# extra.{router2 ⇒ router}
find . -name '*.scala' -type f -exec perl -pi -e 's/(?<=extra\.router)2//g' {} +
```
