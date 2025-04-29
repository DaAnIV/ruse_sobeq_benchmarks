package operations
import enumeration.Program
import enumeration.Store._
import operations.Types.Type
import utils.Condition._

class LessThan extends BoolOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Boolean] = {
    val values = children(0).values.zip(children(1).values).map { case (lhs: Int, rhs: Int) => lhs < rhs }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.Int, Types.Int)

  override def code(childrenCode: Seq[(String, Boolean)]): String =
    parensIfNeeded(childrenCode(0)) + " < " + parensIfNeeded(childrenCode(1))

  override protected val parenless: Boolean = false
}

class GreaterThanEq extends BoolOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Boolean] = {
    val values = children(0).values.zip(children(1).values).map { case (lhs: Int, rhs: Int) => lhs >= rhs }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.Int, Types.Int)

  override def code(childrenCode: Seq[(String, Boolean)]): String =
    parensIfNeeded(childrenCode(0)) + " >= " + parensIfNeeded(childrenCode(1))

  override protected val parenless: Boolean = false
}

/**
  * Abstract class for Int and String equality comparison. Needed for 'IsIncreasing.sy'.
  *
  * @param typ
  *   The type being checked.
  * @tparam T
  *   The type being checked.
  */
abstract class Eq[T](val typ: Type) extends BoolOperation with ReturnsBottom {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Boolean] = {
    val values = children(0).values.asInstanceOf[List[T]].zip(children(1).values.asInstanceOf[List[T]]).map { case (lhs, rhs) =>
      lhs == rhs
    }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(typ, typ)

  override def code(childrenCode: Seq[(String, Boolean)]): String =
    s"${parensIfNeeded(childrenCode.head)} === ${parensIfNeeded(childrenCode(1))}"

  override protected val parenless: Boolean = false
}

class StringEq extends Eq[String](Types.String)

class And extends BoolOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Boolean] = {
    val lhs = children(0).values.asInstanceOf[Seq[Boolean]]
    val rhs = children(1).values.asInstanceOf[Seq[Boolean]]
    val values = lhs.zip(rhs).map { case (x, y) => x && y }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.Bool, Types.Bool)

  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" && ")

  override protected val parenless: Boolean = false
}
