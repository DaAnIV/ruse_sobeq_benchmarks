package operations

object Types extends Enumeration {
  type Type = Value
  val Int = Value("Int")
  val Bool = Value("Bool")
  val String = Value("String")
  val IntArray = Value("Array[Int]")
  val StringArray = Value("Array[String]")
  val IntSet = Value("Set[Int]")
  val StringSet = Value("Set[String]")

  def arrayOf(t: Type): Type = {
    t match {
      case Int    => IntArray
      case String => StringArray
      case t      => throw new IllegalArgumentException(s"Type Array[$t] not supported")
    }
  }

  def setOf(t: Type): Type = {
    t match {
      case Int    => IntSet
      case String => StringSet
      case t      => throw new IllegalArgumentException(s"Type Set[$t] not supported")
    }
  }

  def elementOf(t: Type): Type = {
    t match {
      case IntArray    => Int
      case StringArray => String
      case IntSet      => Int
      case StringSet   => String
      case t           => throw new IllegalArgumentException(s"elementOf called on non-array type: $t")
    }
  }
}
