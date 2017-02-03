package japgolly.scalajs.react

/*
// =====================================================================================================================
  trait GenericComp[P, Ops[x] <: GenericCompOps[x]] {
    def ops: Ops[P]
  }
  trait GenericCompOps[P] {
    def props: P
    //def map[A](f: P => A): GenericComp[A, _] // ???
  }

  implicit def toOps[P, Ops[x] <: GenericCompOps[x]](c: GenericComp[P, Ops]): Ops[P] =
    c.ops
  implicit def toOpsM[P, P0, Ops[x] <: GenericCompOps[x]](c: Mapped[P, P0, Ops]): Ops[P] =
    c.ops

  trait JsComp[P] extends GenericComp[P, JsCompOps] {
//    def map[A](f: P => A) =
//      _jsToMapped(this)(f)
  }
//  trait JsCompOps[P] extends GenericCompOps[P] {
//    def rawCovariant: P
////    override def map[A](f: P => A): JsComp[A]
//  }

  type JsMapped[P, P0] = Mapped[P, P0, JsCompOpsM[?, P0]]
  trait JsCompOpsM[P, P0] extends GenericCompOps[P] {
    def rawCovariant: P
    def map[A](f: P => A): JsMapped[A, P0]
  }
  type JsCompOps[P] = JsCompOpsM[P, P]

  //  type JsMapped[P, P0] = Mapped[P, P0, JsCompOps, JsComp[P0]]
//  type JsCompOps[P] = JsCompOps2[P, P]
//  trait JsCompOps2[P, P0] extends GenericCompOps[P] {
//    def rawCovariant: P
////    def map[A](f: P => A): JsMapped[A, P0]
//  }

  def simpleJsComp[P0]: JsComp[P0] =
    new JsComp[P0] {
      def ops = {
        val self = this
        new JsCompOpsM[P0, P0] {
          override def props = ???
          override def rawCovariant = ???
          override def map[P](f: P0 => P): JsMapped[P, P0] = {
            val selfOps = this
            Mapped[P, P0, JsCompOpsM[?, P0]](
              new JsCompOpsM[P, P0] {
                override def rawCovariant = f(self.rawCovariant)
                override def props = f(self.props)
                override def map[A](f: P => A): JsMapped[A, P0] = {

                  Mapped[A, P0, JsCompOpsM[?, P0]](
                    ???
                  )
                }
              }
            )
          }
        }
      }
    }

  private def _jsToMapped[P, P0](self: JsComp[P0])(f: P0 => P): JsMapped[P, P0] =
    ???
//    Mapped(self, new JsCompOps[P] {
//      override def props = f(self.props)
//      override def rawCovariant = f(self.rawCovariant)
//    })

  private def _jsMappedToMapped[A, P, P0](self: JsMapped[P, P0])(f: P => A): JsMapped[A, P0] =
    Mapped[A, P0, JsCompOpsM[?, P0]](
      new JsCompOpsM[A, P0] {

//        override def props = f(toOps[P, JsCompOpsM[?, P0]](self).props)
//        override def props = f(toOpsM(self).props)
//        override def props = f(self.props)
        override def rawCovariant = ???

        override def map[B](g: A => B): JsMapped[B, P0] =
          _jsMappedToMapped(self)(g compose f)
      }
    )

//  private def _jsToMapped[P, A](self: JsComp[P])(f: P => A): Mapped[A, P, JsCompOps, JsComp[P]] =
//    Mapped(self, new JsCompOps[A] {
//      override def props = f(self.props)
//      override def rawCovariant = f(self.rawCovariant)
//    })
//  private def _mappedToMapped[P, A](self: JsComp[P])(f: P => A): Mapped[A, P, JsCompOps, JsComp[P]] =
//    Mapped(self, new JsCompOps[A] {
//      override def props = f(self.props)
//      override def rawCovariant = f(self.rawCovariant)
//    })

  // TODO Underlying
  case class Mapped[P, P0, Ops[x] <: GenericCompOps[x]](ops: Ops[P]) extends GenericComp[P, Ops]

//  case class Mapped[P, P0, Ops[x] <: GenericCompOps[x], U <: GenericComp[P0, Ops]](underlying: U, ops: Ops[P]) //, mapP: P0 => P)
//      extends GenericComp[P, Ops]

// case class Mapped[P, P0](underlying: Comp[P0], mapP: P0 => P
//  trait MapOps[P, P0] {
//    def map[A](f: P => A): Mapped[A, P0]
//  }
//  implicit def opsSimple[P](c: Comp[P]): Ops[P] with MapOps[P, P] =
//    new Ops[P] with MapOps[P, P] {
//      override def props = ???
//      override def map[A](f: P => A): Mapped[A, P] =
//        Mapped(c, f)
//    }
//
//  implicit def opsMapped[P, P0](c: Mapped[P, P0]): Ops[P] with MapOps[P, P0] =
//    new Ops[P] with MapOps[P, P0] {
//      override def props = c.mapP(c.underlying.props)
//      override def map[A](f: P => A): Mapped[A, P0] =
//        Mapped(c.underlying, f compose c.mapP)
//    }
//
//  test[Comp[X]](_.props).expect[X]
//  test[Comp[X]](_ map xy).expect[Mapped[Y, X]]
//  test[Mapped[X, O]](_.props).expect[X]
//  test[Mapped[X, O]](_ map xy).expect[Mapped[Y, O]]

 */