package operations

import enumeration.Program
import enumeration.Store._
import operations.Types.Type
import utils.Condition._

class IntAddition extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val values = children(0).values.zip(children(1).values).map { case (x: Int, y: Int) => x + y }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = false

  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" + ")

  override val childTypes: List[Type] = List(Types.Int, Types.Int)
}

class IntMultiplication extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    Program(
      this,
      children(0).values.zip(children(1).values).map { case (x: Int, y: Int) => x * y },
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = false
  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" * ")

  override val childTypes: List[Type] = List(Types.Int, Types.Int)
}

class IntSubtraction extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val values = children(0).values.zip(children(1).values).map { case (x: Int, y: Int) => x - y }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = false
  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" - ")

  override val childTypes: List[Type] = List(Types.Int, Types.Int)
}

/**
  * JavaScript does not have built-in integer division, so we need a combination of Math.truc and float division. TODO (kas) Float errors
  * make this... questionable. We might want to remove it, especially if we don't add benchmarks requiring it.
  */
class IntDivision extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val lhs = children(0).values.asInstanceOf[List[Int]]
    val rhs = children(1).values.asInstanceOf[List[Int]]
    val values = lhs.zip(rhs).map { case (x, y) => x / y }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = {
    val lhs = parensIfNeeded(childrenCode(0))
    val rhs = parensIfNeeded(childrenCode(1))
    f"Math.trunc($lhs / $rhs)"
  }

  override val childTypes: List[Type] = List(Types.Int, Types.Int)

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    !children(1).values.contains(0)
}

class IntMod extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val lhs = children(0).values.asInstanceOf[List[Int]]
    val rhs = children(1).values.asInstanceOf[List[Int]]
    val values = lhs.zip(rhs).map { case (x, y) => x % y }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = false
  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" % ")

  override val childTypes: List[Type] = List(Types.Int, Types.Int)

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    !children(1).values.contains(0)
}

class IntUnaryMinus extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val values = children.head.values.map { case x: Int => -x }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = "-" + parensIfNeeded(childrenCode.head)

  override val childTypes: List[Type] = List(Types.Int)
}

class IntIncrementPostfix extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val child = children.head.asInstanceOf[Program[Int]]
    val digger = child.digger.get
    val (values, postCondition) = digger.update(post, values => values.asInstanceOf[List[Int]].map(_ + 1), identity)
    Program(
      this,
      values.asInstanceOf[List[Int]],
      children,
      pre,
      postCondition,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.Int)

  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = parensIfNeeded(childrenCode.head) + "++"

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean =
    children.head.digger.isDefined
}

class IntPowerOperation extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val values = children(0).values.zip(children(1).values).map { case (base: Int, exp: Int) => Math.pow(base, exp).toInt }
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
    children(1).values.asInstanceOf[List[Int]].forall(n => n >= 0) && {
      val bases = children.head.values.asInstanceOf[List[Int]]
      val exps = children(1).values.asInstanceOf[List[Int]]
      bases.zip(exps).forall { case (base, exp) =>
        val res = Math.pow(base, exp)
        Int.MinValue <= res && res <= Int.MaxValue
      }
    }

  override val childTypes: List[Type] = List(Types.Int, Types.Int)
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = f"Math.pow(${childrenCode.map(parensIfNeeded).mkString(", ")})"
}
