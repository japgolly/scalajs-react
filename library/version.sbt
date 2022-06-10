// Remove timestamp from SNAPSHOT versions
ThisBuild / version := {
  val v = (ThisBuild / version).value
  v.replaceFirst("""\+\d{8}-\d{4}(?=-SNAPSHOT)""", "")
}
