```scala
// Api.scala

def useRef                              [A](initialValue: => A)(implicit step: Step): step.Next[UseRef[A]]
def useRefBy                            [A](f: CtxFn[A])(implicit step: Step): step.Next[UseRef[A]]
def useRefBy                            [A](initialValue: Ctx => A)(implicit step: Step): step.Next[UseRef[A]]
def useRefToAnyVdom                     (implicit step: Step): step.Next[Ref.ToAnyVdom]
def useRefToJsComponent                 [F[_], A[_], P1, S1, CT1[-p, +u] <: CtorType[p, u], R <: JsComponent.RawMounted[P0, S0], P0 <: js.Object, S0 <: js.Object, CT0[-p, +u] <: CtorType[p, u
def useRefToJsComponent                 [P <: js.Object, S <: js.Object](implicit step: Step): step.Next[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S]]]
def useRefToJsComponentWithMountedFacade[P <: js.Object, S <: js.Object, F <: js.Object](implicit step: Step): step.Next[Ref.ToJsComponent[P, S, JsComponent.RawMounted[P, S] with F]]
def useRefToScalaComponent              [P, S, B, CT[-p, +u] <: CtorType[p, u
def useRefToScalaComponent              [P, S, B](implicit step: Step): step.Next[Ref.ToScalaComponent[P, S, B]]
def useRefToVdom                        [N <: TopNode: ClassTag](implicit step: Step): step.Next[Ref.ToVdom[N]]

def renderReusable                      [A](f: Ctx => Reusable[A])(implicit s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT]
def renderReusable                      [A](f: CtxFn[Reusable[A]])(implicit step: Step, s: CtorType.Summoner[Box[P], C], v: A => VdomNode): Component[P, s.CT]

def renderWithReuse                     (f: Ctx => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT]
def renderWithReuse                     (f: CtxFn[VdomNode])(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[Ctx]): Component[P, s.CT]
def renderWithReuseBy                   [A](reusableInputs: Ctx => A)(f: A => VdomNode)(implicit s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT]
def renderWithReuseBy                   [A](reusableInputs: CtxFn[A])(f: A => VdomNode)(implicit step: Step, s: CtorType.Summoner[Box[P], C], r: Reusability[A]): Component[P, s.CT]

// Custom hooks need rewriting too - see the JS only example. This is going to be time-consuming... Will have to make it low-priority for now...
```
