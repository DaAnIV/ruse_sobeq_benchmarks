package utils

import scala.collection.mutable

object Condition {
  type Heaplet = (String, List[Any])
  type PreCondition = Condition
  type PostCondition = Condition
  type Effect = Seq[Option[Seq[Any]]]

  implicit class ConditionOps(private val underlying: Iterable[Seq[Any]]) extends AnyVal {
    def toCondition: Condition = {
      Condition(underlying.toList)
    }
  }

  def effect(pre: PreCondition, post: PostCondition): Effect = {
    post.inner.zip(pre.inner).map {
      case (post, pre) if post != pre => Some(post)
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
    if (fst._2 != snd._1) {
      None
    } else {
      Some((fst._1, snd._2))
    }
  }
}

case class Condition(
    private val inner: List[Seq[Any]]
)
{
  @inline
  def variables: Int = this.inner.length

  def apply(varIdx: Int): Seq[Any] = this.inner(varIdx)

  def mutate(varIdx: Int, values: Seq[Any]): Condition = {
    Condition(this.inner.updated(varIdx, values))
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
}
