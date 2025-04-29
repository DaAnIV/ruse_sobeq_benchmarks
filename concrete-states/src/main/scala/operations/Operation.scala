package operations

import context.ContextDigger
import enumeration.Store._
import enumeration.{Program, Store}
import operations.Types.Type
import utils.Condition
import utils.Condition._

/**
  * Base trait for all operations that can be turned into code. (Note that this trait represents _operations_, not concrete nodes in the
  * program space).
  */
trait Codeable {

  /**
    * Creates the concrete syntax for the codeable.
    *
    * @param children
    *   The child nodes this is evaluated with.
    * @return
    *   The concrete syntax representation of this codeable.
    */
  def mkCode(children: Seq[Program[_]]): String =
    code(children.map(child => {
      (child.code, child.op.parenless)
    }))

  /**
    * Creates te concrete syntax for the codeable.
    *
    * @param childrenCode
    *   List of tuples of (code, parenless) for the child nodes this is evaluated with.
    * @return
    *   The concrete syntax representation of this codeable.
    */
  def code(childrenCode: Seq[(String, Boolean)]): String

  /**
    * TODO (kas) ???
    *
    * @param children
    * @return
    */
  // def lhs(children: List[EquivalenceGraphRelation]): Boolean

  /**
    * Creates a context digger for this codeable. Can be unimplemented if `lhs()` returns `false`.
    *
    * @param children
    *   The list of child nodes this is evaluated with.
    * @return
    *   The context digger for this codeable.
    */
  def makeDigger(children: Seq[Program[_]]): Option[ContextDigger]

  /**
    * The type of the return value of this codeable.
    *
    * @return
    *   The type of the return value of this codeable.
    */
  def resultType: Type

  /**
    * Whether the concrete syntax for this codeable does _not_ need to be wrapped in parentheses when combined with other operations.
    */
  protected val parenless: Boolean

  /**
    * Utility function. Wraps `p._1` with parenthesis if `p._2` is `false` (see `code` above for why this is useful).
    */
  def parensIfNeeded(p: (String, Boolean)): String = if (p._2) p._1 else s"(${p._1})"
}

/**
  * Trait for codeables that are method calls, with the first child the object, and the rest arguments to the method.
  *
  * This is a utility trait to avoid duplicate `code` implementations.
  */
trait MethodCall extends Codeable {
  protected val parenless: Boolean = true
  protected val name: String

  def code(childrenCode: Seq[(String, Boolean)]): String = {
    val obj = childrenCode.head
    val args = childrenCode.tail
    s"${parensIfNeeded(obj)}.$name(${args.map(_._1).mkString(",")})"
  }
}

trait Operation extends Codeable {

  /**
    * Returns the return values and new context arising from applying this operation with the given children, in the given contexts.
    *
    * It can assume that both arguments are valid (i.e. `canApply(children)` returns `true`), and perform no checks.
    *
    * @param children
    *   The child nodes for this operation.
    * @param existingContexts
    *   The contexts this operation starts evaluating in.
    * @return
    *   The return value of this operation, and the new context resulting from it.
    */
  def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_]

  /**
    * Used to specify operation-specific checks for children and contexts (aside from those already checked in `canApply` for all
    * operations.
    *
    * @param children
    *   The child nodes for the operation.
    * @param existingContexts
    *   The context the operation starts evaluating in.
    * @return
    *   Whether the given children and contexts are valid for this operation.
    */
  @inline
  def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  /**
    * Checks if the given children and contexts are valid for this operation. Specifically:
    *   - The children are the correct length and types
    *   - The children's context form a valid chain of context transformations
    *   - this node will be evaluated in the context (`existingContexts`) after evaluating the last child.
    *
    * @param children
    *   The list of child node for this operation.
    * @param existingContexts
    *   The contexts _this_ operation will be evaluated in.
    * @return
    *   Whether the given children and contexts are valid for this operation.
    */
  def canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = _canApply(children, cond)
  // {
  //   children.length == childTypes.length &&
  //   children.zip(childTypes).forall { case (eqr, t) => eqr.typ == t } &&
  //   _canApply(children, cond) &&
  //   children.zip(children.tail).forall { case (firstChildClass: EquivalenceGraphRelation, secondChildClass: EquivalenceGraphRelation) =>
  //     firstChildClass.contexts.last == secondChildClass.contexts.head
  //   } &&
  //   children.last.contextAfter == existingContexts
  // }

  /**
    * The return type of valid children for this operation.
    */
  val childTypes: List[Types.Type]
}

/**
  * Base trait for operations that don't have any children (e.g. variables).
  */
trait LeafOperation extends Codeable {
  def apply(cond: Condition): Program[_]
}

//type specialized
trait ReturnsInt extends Codeable {
  val resultType: Type = Types.Int
}

trait ReturnsString extends Codeable {
  val resultType: Type = Types.String
  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = None
}

trait ReturnsIntArray extends Codeable {
  val resultType: Type = Types.IntArray
}

trait ReturnsStringArray extends Codeable {
  val resultType: Type = Types.StringArray
}

trait ReturnsIntSet extends Codeable {
  val resultType: Type = Types.IntSet
}

trait ReturnsStringSet extends Codeable {
  val resultType: Type = Types.StringSet
}

trait ReturnsBool extends Codeable {
  val resultType: Type = Types.Bool
}

trait ReturnsBottom extends Codeable {
  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = None
}

trait LiteralOperation extends LeafOperation with ReturnsBottom {}

trait IntOperation extends Operation with ReturnsInt {}

trait IntLeafOperation extends LeafOperation with ReturnsInt {}

trait StringOperation extends Operation with ReturnsString {}

trait StringLeafOperation extends LeafOperation with ReturnsString {}

trait ListLeafOperation[T] extends LeafOperation {}

trait ListOperation[T] extends Operation {}

trait SetOperation[T] extends Operation {}

trait SetLeafOperation[T] extends LeafOperation {}

trait BoolOperation extends Operation {

  val resultType: Type = Types.Bool
}

trait BoolLeafOperation extends LeafOperation with ReturnsBool {}

object VariableOperation {
  def makeVariable[T](name: String, idx: Int, typ: Types.Type, cond: Condition, variables: Int): Program[T] = {
    val op = typ match {
      case Types.Int         => new IntVariable(name, idx)
      case Types.String      => new StringVariable(name, idx)
      case Types.Bool        => new BoolVariable(name, idx)
      case Types.IntArray    => new IntArrayVariable(name, idx)
      case Types.StringArray => new StringArrayVariable(name, idx)
      case Types.IntSet      => new IntSetVariable(name, idx)
      case Types.StringSet   => new StringSetVariable(name, idx)
    }

    op(cond).asInstanceOf[Program[T]]
  }
}

abstract class VariableOperation[T](val varname: String, val varIdx: Int) extends LeafOperation {
  def apply(cond: Condition): Program[T] = {
    // assume(cond.contains(varname))

    val digger = this.makeDigger(Nil)
    Program(
      this,
      cond(varIdx).asInstanceOf[List[T]],
      Nil,
      cond,
      cond,
      digger
    )
  }

  override def makeDigger(children: Seq[Program[_]]): Some[ContextDigger]

  override protected val parenless: Boolean = true
}
