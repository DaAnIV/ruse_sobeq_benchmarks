package enumeration

import context.ContextDigger
import enumeration.Store._
import operations.Codeable
import operations.Types.Type
import utils.Condition

object Program {
  type Address = Option[String]
}

case class Program[T <: Any](
    op: Codeable,
    values: Seq[T],
    children: Seq[Program[_]],
    preCondition: Condition,
    postCondition: Condition,
    digger: Option[ContextDigger]
) {
  val typ: Type = op.resultType
  def address: Option[String] = digger.map(_.xpath)
  def currentValues(condition: Condition): Seq[T] =
    this.digger.map(_.get(condition).asInstanceOf[List[T]]).getOrElse(this.values) // TODO Is this necessary?

  def code: String = this.op.mkCode(this.children)

  def allNodes(): List[Program[_]] =
    this :: this.children.flatMap(_.allNodes).toList

  @inline
  def isMut(): Boolean =
    this.allNodes.exists(node => !node.preCondition.equals(node.postCondition))

  @inline
  def effects(): Seq[Option[Seq[Any]]] = Condition.effect(this.preCondition, this.postCondition)
}
