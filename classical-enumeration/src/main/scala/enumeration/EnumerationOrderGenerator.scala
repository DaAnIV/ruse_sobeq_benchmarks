package enumeration

object EnumerationOrderGenerator {
  var currentOrder: String = "rr"

  private var ordering: EnumOrder = new ExponentialBackoffOrdering(5)

  def setEnumerationOrder(config: String): Unit = {
    currentOrder = config
    ordering = config.take(3) match {
      case "rr"  => new RoundRobinOrdering
      case "lin" => new LinearBackoffOrdering(config.drop(3).toInt)
      case "exp" => new ExponentialBackoffOrdering(config.drop(3).toInt)
      case _ =>
        println(s"Failed to parse enumeration order: $config")
        new ExponentialBackoffOrdering(5)
    }
  }

  def isValidOrder(config: String): Boolean = config.take(3) match {
    case "rr"  => true
    case "lin" => config.drop(3).toIntOption.isDefined
    case "exp" => config.drop(3).toIntOption.isDefined
    case _     => false
  }
}

trait EnumOrder {
}

class RoundRobinOrdering extends EnumOrder {
}

class LinearBackoffOrdering(coefficient: Int) extends EnumOrder {
}

class ExponentialBackoffOrdering(base: Int) extends EnumOrder {
}
