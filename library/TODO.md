```scala
// Api.scala

def renderReusable                      [A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT]
def renderReusable                      [A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT]

def renderWithReuse                     (f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT]
def renderWithReuse                     (f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT]
def renderWithReuseBy                   [A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT]
def renderWithReuseBy                   [A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT]

// Custom hooks need rewriting too - see the JS only example. This is going to be time-consuming... Will have to make it low-priority for now...
```
