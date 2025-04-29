package operations
import context.{ContextDigger, StringVarDigger}
import enumeration.Program

class StringVariable(override val varname: String, override val varIdx: Int) extends VariableOperation[String](varname, varIdx) with ReturnsString {

  override def code(childrenCode: Seq[(String, Boolean)]): String = varname

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger] =
    Some(new StringVarDigger(varname, varIdx))

  override protected val parenless: Boolean = true
}
