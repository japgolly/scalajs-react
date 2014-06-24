package golly.react

//trait Component {
//  self =>
//
//  type Self <: Component {type Self = self.Self}
//  type P <: Props
//  type S <: State
//  type Backend
//
//  def newBackend: Backend
//  def spec: Spec
//
//  // -------------------------------------------------------
//
//  final type Scope = ComponentScope[Self]
//  final type Spec = ComponentSpec[Self]
//  final type Constructor = ComponentConstructor[Self]
//
//  protected final def specBuilder = new ComponentSpecBuilder.Init[Self](newBackend.asInstanceOf[Self#Backend])
//
//  def create: Constructor = React.createClass(spec)
//}
