package operations

import enumeration.Program
import utils.Condition

class StringLiteral(val value: String, val examples: Int) extends StringLeafOperation with LiteralOperation {

  override def apply(cond: Condition): Program[String] = Program(
    this,
    List.fill(examples)(this.value),
    Nil,
    cond,
    cond,
    None
  )

  override def code(childrenCode: Seq[(String, Boolean)]): String = {
    val lit = new org.mozilla.javascript.ast.StringLiteral()
    lit.setValue(value)
    lit.setQuoteCharacter('\'')
    lit.getValue(true)
  }

  override protected val parenless: Boolean = true
}
