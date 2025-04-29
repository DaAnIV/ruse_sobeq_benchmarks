package operations
import enumeration.Program
import utils.Condition

abstract class ArrayLiteral[T](val lit: List[T], val examples: Int) extends ListLeafOperation[T] with LiteralOperation {
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = lit.mkString("[", ",", "]")

  override def apply(preCondition: Condition): Program[List[T]] = {
    val values = List.fill(examples)(this.lit)
    Program(
      this,
      values,
      Nil,
      preCondition,
      preCondition,
      None // Literals don't have an address
    )
  }
}

class IntArrayLiteral(lit: List[Int], override val examples: Int) extends ArrayLiteral[Int](lit, examples) with ReturnsIntArray
class StringArrayLiteral(lit: List[String], override val examples: Int) extends ArrayLiteral[String](lit, examples) with ReturnsStringArray
