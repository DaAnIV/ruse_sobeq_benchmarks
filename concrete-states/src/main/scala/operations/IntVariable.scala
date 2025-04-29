package operations

import context.{ContextDigger, IntVarDigger}
import enumeration.Program

class IntVariable(override val varname: String, override val varIdx: Int) extends VariableOperation[Int](varname, varIdx) with ReturnsInt {

  override def code(childrenCode: Seq[(String, Boolean)]): String = varname

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger] = Some(new IntVarDigger(varname, varIdx))
}
