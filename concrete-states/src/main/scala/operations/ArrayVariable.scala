package operations
import context.{ContextDigger, IntArrayVarDigger}
import enumeration.Program

abstract class ArrayVariable[T](override val varname: String, override val varIdx: Int) extends VariableOperation[List[T]](varname, varIdx) with ListLeafOperation[T] {
  override def code(childrenCode: Seq[(String, Boolean)]): String = varname

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger] = Some(new IntArrayVarDigger(varname, varIdx))
}

class IntArrayVariable(override val varname: String, override val varIdx: Int) extends ArrayVariable[Int](varname, varIdx) with ReturnsIntArray
class StringArrayVariable(override val varname: String, override val varIdx: Int) extends ArrayVariable[String](varname, varIdx) with ReturnsStringArray
