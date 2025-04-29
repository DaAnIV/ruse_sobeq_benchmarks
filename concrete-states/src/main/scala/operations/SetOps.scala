package operations
import context.{ArrayDereferenceDigger, ArrayDigger, ContextDigger}
import enumeration.Program
import enumeration.Store.Index
import operations.Types.{IntArray, StringArray, Type, elementOf}
import utils.Condition._

abstract class SetSize[T](elemTyp: Types.Type) extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] =
    Program(
      this,
      children(0).values.map { case s: Set[_] => s.size },
      children,
      pre,
      post,
      None
    )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(Types.setOf(elemTyp))
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = parensIfNeeded(childrenCode.head) + ".length"
}

class IntSetSize extends SetSize[Int](Types.Int)
class StringSetSize extends SetSize[String](Types.String)

abstract class SetAdd[T](elemType: Types.Type) extends SetOperation[T] with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val set = children(0).asInstanceOf[Program[Set[T]]]
    val elem = children(1).asInstanceOf[Program[T]]
    val adder = (s: Seq[Any]) => s.asInstanceOf[Seq[Set[T]]].zip(elem.values).map { case (a, e) => a + e }
    val (values, postCondition) = (set.digger match {
      case Some(digger) =>
        digger.update(
          post,
          adder,
          adder
        )
      case None =>
        (adder(set.values), post)
    }).asInstanceOf[(List[Int], PostCondition)]

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      None
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    // TODO Should we only allow this for sets which are lhs?
    true

  override val childTypes: List[Type] = List(Types.setOf(elemType), elemType)

  override def resultType: Type = childTypes.head

  override protected val name: String = "add"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = children.head.digger
}

class IntSetAdd extends SetAdd[Int](Types.Int)
class StringSetAdd extends SetAdd[String](Types.String)

abstract class SetDelete[T](elemType: Types.Type) extends SetOperation[T] with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val set = children(0).asInstanceOf[Program[Set[T]]]
    val elem = children(1).asInstanceOf[Program[T]]

    val deleter = (s: Seq[Any]) => s.asInstanceOf[Seq[Set[T]]].zip(elem.values).map { case (a, e) => a - e }
    val haser = (s: Seq[Any]) => s.asInstanceOf[Seq[Set[T]]].zip(elem.values).map { case (a, e) => a.contains(e) }

    val (values, postCondition) = (set.digger match {
      case Some(digger) =>
        digger.update(
          post,
          deleter,
          haser
        )
      case None =>
        (haser(set.values), post)
    }).asInstanceOf[(List[Int], PostCondition)]

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      None
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    // TODO Should we only allow this for sets which are lhs?
    true

  override val childTypes: List[Type] = List(Types.setOf(elemType), elemType)

  override def resultType: Type = Types.Bool

  override protected val name: String = "delete"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = None
}

class IntSetDelete extends SetDelete[Int](Types.Int)
class StringSetDelete extends SetDelete[String](Types.String)

abstract class SetHas[T](elemType: Types.Type) extends SetOperation[T] with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val set = children(0).asInstanceOf[Program[Set[T]]]
    val elem = children(1).asInstanceOf[Program[T]]

    val values = set.currentValues(post).zip(elem.values).map { case (a, e) => a.contains(e) }

    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    // TODO Should we only allow this for sets which are lhs?
    true

  override val childTypes: List[Type] = List(Types.setOf(elemType), elemType)

  override def resultType: Type = Types.Bool

  override protected val name: String = "has"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = None
}

class IntSetHas extends SetHas[Int](Types.Int)
class StringSetHas extends SetHas[String](Types.String)
