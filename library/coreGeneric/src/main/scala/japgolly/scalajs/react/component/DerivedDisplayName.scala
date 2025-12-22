package japgolly.scalajs.react.component

import sourcecode.FullName

trait DerivedDisplayName {
  protected def derivedDisplayName(implicit name: FullName): String = {
    // Heuristic to split the name into package and class name
    val parts = name.value.split('.')
    val (packageName, className) = parts.span(_.headOption.forall(_.isLower))
    className.mkString(".") + Some(packageName).filter(_.nonEmpty).fold("")(_.mkString(" (", ".", ")"))
  }
}
