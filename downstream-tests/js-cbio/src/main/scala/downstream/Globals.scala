package downstream

object Globals {

  var catnipMounts = List.empty[String]

  def clear(): Unit = {
    catnipMounts = Nil
  }

  clear()
}
