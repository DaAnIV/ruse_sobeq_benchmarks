package operations

import operations.Types.Type
import utils.Condition._
import enumeration._
import enumeration.Store._

abstract class Sequence extends Operation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = Program(
    this,
    children.last.values,
    children,
    pre,
    post,
    None // Sequence is not a valid lhs!
  )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = {
    val first = children.head
    first.preCondition != first.postCondition
  }

  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(_._1).mkString("(", ", ", ")")

  override def resultType: Type = childTypes.last

  override protected val parenless: Boolean = false

  override def toString: String = this.childTypes.map(t => f"<$t expr>").mkString(", ")
}

/**
  * This is a special class used *only* in the PostProcessor. NEVER build this as part of enumeration!
  */
class InfiniteSequence extends Operation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = ???

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = ???

  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(_._1).mkString(", ")

  override val childTypes: List[operations.Types.Type] = Nil

  override def resultType: Type = null

  override protected val parenless: Boolean = false

  override def toString: String = this.childTypes.map(t => f"<$t expr>").mkString(", ")
}

class Assignment(val varName: String, val varIdx: Int, val varType: Type) extends Operation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val values = children.last.currentValues(post)
    Program(this, values, children, pre, post, None)
  }

  override def code(childrenCode: Seq[(String, Boolean)]): String = f"${this.varName} = ${childrenCode.last._1}"

  override def resultType: Type = childTypes.last

  override val childTypes: List[Type] = List(this.varType)

  override protected val parenless: Boolean = false

  override def toString: String = f"${this.varName} = <${this.varType}>"
}
