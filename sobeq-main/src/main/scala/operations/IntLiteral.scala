package operations

import enumeration.Program
import utils.Condition

class IntLiteral(val lit: Int, val examples: Int) extends IntLeafOperation with LiteralOperation {
  override def apply(cond: Condition): Program[Int] =
    Program(
      this,
      List.fill(examples)(this.lit),
      Nil,
      Condition.emp(cond.variables),
      Condition.emp(cond.variables),
      None
    )

  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = lit.toString
}
