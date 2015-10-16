package japgolly.scalajs.react

import scala.scalajs.js

trait ElementFactory {

  /**
   * add types to js constructor
   * @param ctor
   * @tparam P
   * @tparam S
   * @return
   */
  def getComponentConstructor[P, S, C <: ReactComponent[P, S]](ctor: js.Dynamic): ReactComponentConstructor[P, S, C] = {
    ctor.asInstanceOf[ReactComponentConstructor[P, S, C]]
  }

  def getStatelessFactory[P](fn: js.Function1[P, ReactElement]) = React.createFactory((props: JSProps[P]) => fn(props.sprops)).asInstanceOf[ReactComponentFactory[P, _]]

  /**
   * helper method to create ReactElements for components with props
   * @param reactClass typed constructor
   * @param props props of react component
   * @param key
   * @param ref
   * @tparam P
   * @tparam S
   * @return
   */
  def createElement[P, S](reactClass: ReactClass[P, S, _, _],
                          props: P,
                          key: js.UndefOr[String] = js.undefined,
                          ref: js.Function1[_ <: ReactComponent[P, S], _] = null
                         ) = createElementWithChildren(reactClass, props, key, ref)()


  /**
   * helper method to create ReactElements for components with no props
   * @param reactClass typed constructor
   * @param key
   * @param ref
   * @tparam P
   * @tparam S
   * @return
   */
  def createElementNoProps[P, S](reactClass: ReactClass[P, S, _, _],
                                 key: js.UndefOr[String] = js.undefined,
                                 ref: js.Function1[_ <: ReactComponent[P, S], _] = null
                                ) = createElementNoPropsWithChildren(reactClass, key, ref)()

  /**
   * helper method to create ReactElements for components with props  and children
   * @param reactClass typed constructor
   * @param props
   * @param key
   * @param ref
   * @param children
   * @tparam P
   * @tparam S
   * @return
   */
  def createElementWithChildren[P, S](reactClass: ReactClass[P, S, _, _],
                                      props: P,
                                      key: js.UndefOr[String] = js.undefined,
                                      ref: js.Function1[_ <: ReactComponent[P, S], _] = null
                                     )(children: ReactNode*): ReactElementU[P, S] =
    React.createElement(reactClass, JSProps(key, if (ref != null) ref else js.undefined, props), children: _*).asInstanceOf[ReactElementU[P, S]]


  /**
   * helper method to create ReactElements for components with no props and children
   * @param reactClass typed constructor
   * @param key
   * @param ref
   * @param children
   * @tparam P
   * @tparam S
   * @return
   */
  def createElementNoPropsWithChildren[P, S](reactClass: ReactClass[P, S, _, _],
                                             key: js.UndefOr[String] = js.undefined,
                                             ref: js.Function1[_ <: ReactComponent[P, S], _] = null
                                            )(children: ReactNode*): ReactElementU[P, S] =
    React.createElement(reactClass, JSProps(key, if (ref != null) ref else js.undefined, ()), children: _*).asInstanceOf[ReactElementU[P, S]]


}

object ElementFactory extends ElementFactory