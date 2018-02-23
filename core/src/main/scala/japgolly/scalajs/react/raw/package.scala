package japgolly.scalajs.react

import scalajs.js
import scalajs.js.|

/** See https://flow.org/en/docs/react/types */
package object raw {

  type Void = Unit

  type JsNumber = Byte | Short | Int | Float | Double

  type Empty = Void | Null | Boolean

  val recursiveTypeAliases: RecursiveTypeAliases = RecursiveTypeAliases
  type ChildrenArray[A] = A | recursiveTypeAliases.ChildrenArray[A]

  type PropsChildren = React.Node

  @js.native
  trait PropsWithChildren extends js.Object {
    val children: PropsChildren
  }












  // Deprecated by React
  type Ref = String | Null

  type RefFn = js.Function1[js.Any, Unit]


  @js.native
  trait ReactDOMElement extends React.Element {
    def `type`: String
    def props: PropsWithChildren
    def key: Key
    def ref: Ref
  }

  @js.native
  trait ReactComponentElement[P <: js.Object] extends React.Element {
    def `type`: React.Constructor[P]
    def props: P with PropsWithChildren
    def key: Key
    def ref: Ref
  }

  // Type aliases can't be recursive
  // type ReactFragment = js.Array[React.Node | ReactEmpty]
//  @js.native
//  trait ReactFragment extends js.Any
//  implicit def ReactFragment[A](a: A)(implicit w: A => js.Array[React.Node | ReactEmpty]): ReactFragment =
//    w(a).asInstanceOf[ReactFragment]

//  def emptyReactNodeList: ReactNodeList =
//    js.undefined

  type ReactClass[P <: js.Object, S <: js.Object] = js.Function1[P, React.Component[P, S]] with HasDisplayName
  type ReactClassP[P <: js.Object] = ReactClass[P, _ <: js.Object]
  type ReactClassUntyped = ReactClass[_ <: js.Object, _ <: js.Object]

  @js.native
  trait HasDisplayName extends js.Object {
    val displayName: js.UndefOr[String] = js.native
  }

}

