// package japgolly.scalajs.react.component

// import japgolly.scalajs.react.vdom.VdomNode

// sealed trait Renderable[A]{
//   def render(a: A): VdomNode
// }

// implicit object RenderableVdomNode extends Renderable[VdomNode] {
//   def render(a: VdomNode): VdomNode = a
// }

// implicit object RenderableHookResult extends Renderable[HookResult[VdomNode]]] {
//   def render(a: String): VdomNode = VdomNode(a)
// }
