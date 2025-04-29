package utils

import scala.collection.mutable

object Condition {
  type Heaplet = (String, List[Any])
  type PreCondition = Condition
  type PostCondition = Condition
  type Effect = Seq[Option[Seq[Any]]]

  implicit class ConditionOps(private val underlying: Iterable[Option[Seq[Any]]]) extends AnyVal {
    def toCondition: Condition = {
      assume(underlying.nonEmpty)
      Condition(underlying.toList)
    }
  }

  def emp(variables: Int): Condition = {
    Condition(List.fill(variables)(None))
  }

  def effect(pre: PreCondition, post: PostCondition): Effect = {
    post.inner.zip(pre.inner).map {
      case (post, pre) if post != pre => post
      case _                          => None
    }
  }

  // The initial state. Must be set at the start of enumeration.
  // TODO This is bad bad no good bad bad bad
  var initialState: Condition = _

  /**
    * This method takes the pre- and post-condition pairs of a sequence of child nodes, and tries to apply the frame rule to (a) prove that
    * they can be sequenced, and (b) to compute the overall pre- and post-conditions. It also returns the total number of mutations that
    * occur in the sequence.
    */
  def makeValid(fst: (PreCondition, PostCondition), snd: (PreCondition, PostCondition)): Option[(PreCondition, PostCondition)] = {
    val preCondition = fst._1.inner.toBuffer
    val postCondition = fst._2.inner.toBuffer

    for (i <- Range(0, preCondition.length)) {
      if (postCondition(i).isEmpty) {
        preCondition(i) = snd._1.inner(i)
      }

      if (snd._2.inner(i).isDefined) {
        postCondition(i) = snd._2.inner(i)
      }
    }

    Some((Condition(preCondition.toList), Condition(postCondition.toList)))
  }
}

case class Condition(
    private val inner: List[Option[Seq[Any]]]
) {
  @inline
  def variables: Int = this.inner.length

  def contains(varIdx: Int): Boolean = this.inner(varIdx).isDefined

  def apply(varIdx: Int): Option[Seq[Any]] = this.inner(varIdx)

  def toVariable(varIdx: Int): Condition = {
    Condition(
      List.fill(this.inner.length)(None).updated(varIdx, this.inner(varIdx))
    )
  }

  def mutate(varIdx: Int, values: Seq[Any]): Condition = {
    Condition(this.inner.updated(varIdx, Some(values)))
  }

  def maybeSatisfiableBy(other: Condition): Boolean = this.inner.zip(other.inner).forall { case (self, other) =>
    self.isEmpty || other.isEmpty || self == other
  }

  def implies(other: Condition): Boolean = this.inner.zip(other.inner).forall { case (self, other) =>
    other.isEmpty || self == other
  }

  override def toString(): String = this.inner.mkString("Condition [", "; ", "]")

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Condition]

  override def equals(x: Any): Boolean = this.canEqual(x) && x.asInstanceOf[Condition].inner == this.inner

  def isEmpty: Boolean = this.inner.forall(_.isEmpty)

  def subsets(): Seq[Condition] = {
    val rs = this.inner.zipWithIndex
      .filter(_._1.isDefined)
      .toSet
      .subsets()
      .map(subset => {
        val rs: mutable.Buffer[Option[Seq[Any]]] = mutable.ArrayBuffer.fill(this.inner.length)(None)
        for ((value, idx) <- subset) {
          rs(idx) = value
        }
        Condition(rs.toList)
      })
      .toList
    rs
  }
}
