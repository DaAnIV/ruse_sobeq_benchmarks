package operations

import context.{BoolVarDigger, ContextDigger}
import enumeration._
import utils.Condition

class BoolVariable(override val varname: String, override val varIdx: Int) extends VariableOperation[Boolean](varname, varIdx) with BoolLeafOperation {
  override def apply(cond: Condition): Program[Boolean] = {
    Program(
      this,
      cond(this.varIdx).asInstanceOf[List[Boolean]],
      Nil,
      cond,
      cond,
      this.makeDigger(Nil)
    )
  }

  override def code(childrenCode: Seq[(String, Boolean)]): String = varname

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger] =
    Some(new BoolVarDigger(varname, this.varIdx))

  override protected val parenless: Boolean = true
}
