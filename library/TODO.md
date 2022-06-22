```scala
// Api.scala

def useContext                          [A](ctx: Context[A])(implicit step: Step): step.Next[A]
def useContextBy                        [A](f: Ctx => Context[A])(implicit step: Step): step.Next[A]
def useContextBy                        [A](f: CtxFn[Context[A]])(implicit step: Step): step.Next[A]

def useDebugValue                       (desc: => Any)(implicit step: Step): step.Self
def useDebugValueBy                     (desc: Ctx => Any)(implicit step: Step): step.Self
def useDebugValueBy                     (f: CtxFn[Any])(implicit step: Step): step.Self

def useEffect                           [A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useEffectBy                         [A](init: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useEffectBy                         [A](init: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self
def useEffectOnMount                    [A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useEffectOnMountBy                  [A](effect: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useEffectOnMountBy                  [A](effect: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self
def useForceUpdate                      (implicit step: Step): step.Next[Reusable[DefaultEffects.Sync[Unit]]]

def useEffectWithDeps                   [D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
def useEffectWithDepsBy                 [D, A](deps: Ctx => D)(effect: Ctx => D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
def useEffectWithDepsBy                 [D, A](deps: CtxFn[D])(effect: CtxFn[D => A])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self

def useLayoutEffect                     [A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useLayoutEffectBy                   [A](init: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useLayoutEffectBy                   [A](init: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self
def useLayoutEffectOnMount              [A](effect: A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useLayoutEffectOnMountBy            [A](effect: Ctx => A)(implicit a: UseEffectArg[A], step: Step): step.Self
def useLayoutEffectOnMountBy            [A](effect: CtxFn[A])(implicit a: UseEffectArg[A], step: Step): step.Self

def useLayoutEffectWithDeps             [D, A](deps: => D)(effect: D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
def useLayoutEffectWithDepsBy           [D, A](deps: Ctx => D)(effect: Ctx => D => A)(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self
def useLayoutEffectWithDepsBy           [D, A](deps: CtxFn[D])(effect: CtxFn[D => A])(implicit a: UseEffectArg[A], r: Reusability[D], step: Step): step.Self

def useReducer                          [S, A](reducer: (S, A) => S, initialState: => S)(implicit step: Step): step.Next[UseReducer[S, A]]
def useReducerBy                        [S, A](reducer: Ctx => (S, A) => S, initialState: Ctx => S)(implicit step: Step): step.Next[UseReducer[S, A]]
def useReducerBy                        [S, A](reducer: CtxFn[(S, A) => S], initialState: CtxFn[S])(implicit step: Step): step.Next[UseReducer[S, A]]

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
