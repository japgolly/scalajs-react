package japgolly.scalajs.react.test

import japgolly.scalajs.react._

/**
  * Renders a component into the document so that
  *
  * - you can easily change props and/or state.
  * - it is unmounted when the test is over.
  *
  * @since 0.10.5
  */
abstract class ComponentTester[P, S, B, N <: TopNode] {
  def component: ReactComponentM[P, S, B, N]
  def setProps(props: P): Unit
  def setState(state: S): Unit
}

object ComponentTester {
  def apply[P, S, B, N <: TopNode, A](comp: ReactComponentC.ReqProps[P, S, B, N])
                                     (initialProps: P)
                                     (f: ComponentTester[P, S, B, N] => A): A =
    new Manual(initialProps)(comp(_)).test(f)


  class Manual[P, S, B, N <: TopNode](initialProps: P)(render: P => ReactComponentU[P, S, B, N])
      extends ComponentTester[P, S, B, N] {

    var $: ReactComponentM[P, S, B, N] =
      ReactTestUtils.renderIntoDocument(render(initialProps))

    override def component = $

    override def setProps(props: P): Unit =
      $ = ReactDOM.render(render(props), containerDom)

    override def setState(state: S): Unit =
      component.setState(state)

    def containerDom =
      component.getDOMNode().parentNode

    def unmount(): Unit =
      ReactDOM unmountComponentAtNode containerDom

    def test[A](f: this.type => A): A =
      try f(this)
      finally unmount()
  }
}
