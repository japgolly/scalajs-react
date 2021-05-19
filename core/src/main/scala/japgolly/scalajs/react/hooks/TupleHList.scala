package japgolly.scalajs.react.hooks

object TupleHList {

  object Empty
  type Empty = Empty.type

  trait Append[A, B] {
    type Result
    val append: (A, B) => Result
  }

  trait AppendLowest {
    implicit def tuple2[A, B]: Append.To[A, B, (A, B)] =
      Append((_, _))
  }
  trait AppendEmptyL extends AppendLowest {
    implicit def emptyL[A]: Append.To[Empty, A, A] =
      Append((_, a) => a)
  }
  trait AppendEmptyR extends AppendEmptyL {
    implicit def emptyR[A]: Append.To[A, Empty, A] =
      Append((a, _) => a)
  }

  object Append extends AppendEmptyR {
    type To[A, B, Z] = Append[A, B] { type Result = Z }

    def apply[A, B, Z](f: (A, B) => Z): To[A, B, Z] =
      new Append[A, B] {
        override type Result = Z
        override val append = f
      }
  }



}
