package golly.react

trait Component {
  self =>

  type Self <: Component {type Self = self.Self}
  type P <: Props
  type S <: State

  final type Scope = ComponentScope[Self]
  final type Spec = ComponentSpec[Self]
  final type Constructor = ComponentConstructor[Self]

  def spec: Spec
  protected final def specBuilder = ComponentSpecBuilder.apply[Self]

  def create: Constructor = React.createClass(spec)
}
