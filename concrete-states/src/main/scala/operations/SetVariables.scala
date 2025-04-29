package operations

import context.{ContextDigger, IntSetVarDigger}
import enumeration.Program

abstract class SetVariable[T](override val varname: String, override val varIdx: Int) extends VariableOperation[Set[T]](varname, varIdx) with SetLeafOperation[T] {
  override def code(childrenCode: Seq[(String, Boolean)]): String = varname

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger] = Some(new IntSetVarDigger(varname, varIdx))
}

class IntSetVariable(override val varname: String, override val varIdx: Int) extends SetVariable[Int](varname, varIdx) with ReturnsIntSet
class StringSetVariable(override val varname: String, override val varIdx: Int) extends SetVariable[String](varname, varIdx) with ReturnsStringSet
