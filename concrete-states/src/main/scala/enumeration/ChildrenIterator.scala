package enumeration

import enumeration.Store._
import operations.{Operation, Types}
import utils.Condition
import utils.Condition._

import scala.concurrent.duration.Deadline
import scala.collection.mutable
import scala.annotation.tailrec

class ChildIterator(
    val iter: Iterator[Program[_]],
    val pre: Option[PreCondition],
    val post: Option[PostCondition]
) {
  var candidate: Option[(Program[_], PreCondition, PostCondition)] = None

  def getCurrent: Option[(Program[_], PreCondition, PostCondition)] = this.candidate

  def iterate(): Boolean = {
    var success = false
    while (!success && this.iter.hasNext) {
      val prog = iter.next()
      if (pre.isDefined && post.isDefined) {
        Condition.makeValid((pre.get, post.get), (prog.preCondition, prog.postCondition)) match {
          case Some((pre, post)) =>
            // success
            this.candidate = Some((prog, pre, post))
            success = true
          case _ => ()
        }
      } else {
        success = true
        this.candidate = Some((prog, prog.preCondition, prog.postCondition))
      }
    }
    success
  }
}

object ChildrenIterator {
  type Args = (Seq[Program[_]], PreCondition, PostCondition)
  var useSeenSet: Boolean = false // TODO This doesn't apply anymore?
}

class ChildrenIterator(
    val previousLevelChildren: Iterable[(PreCondition, mutable.Buffer[Program[_]])],
    val vocab: Operation,
    val deadline: Deadline,
    val newChildrenStartIndex: Int,
    val variables: Int
) extends Iterator[ChildrenIterator.Args] {

  import ChildrenIterator._

  val childCount = this.vocab.childTypes.length
  var iterators: Array[ChildIterator] = new Array(this.childCount)
  var candidate: Option[Args] = None

  // Setup
  // -------------------------------------------------------------------------------------
  // Fill up the iterators array
  resetItersForward(0, None, None)
  // -------------------------------------------------------------------------------------

  override def hasNext: Boolean = {
    if (this.candidate.isEmpty) {
      this.nextChildren()
    }
    this.candidate.isDefined
  }

  override def next(): (Seq[Program[_]], PreCondition, PostCondition) = {
    if (this.candidate.isEmpty) {
      this.nextChildren()
    }
    val rs = this.candidate.get
    this.candidate = None
    rs
  }

  @tailrec
  final def nextChildren(): Unit = {
    var i = this.childCount - 1
    while (i >= 0 && this.deadline.hasTimeLeft() && !this.tryIterateForward(i)) {
      i -= 1
    }

    // There are no more children
    if (i < 0 || this.deadline.isOverdue()) return

    do {
      val tups = this.iterators.map(_.getCurrent.get)
      val indices = tups.map(_._2)

      // if (indices.exists(_ > this.newChildrenStartIndex)) {
      val (_, pre, post) = tups.last
      val programs = tups.map(_._1)
      if (this.vocab.canApply(programs, post)) {
        this.candidate = Some(
          programs,
          pre,
          post
        )
        return
      }
      // }
    } while (this.tryIterateForward(this.childCount - 1))

    // This failed. So retry if we can.
    if (this.deadline.hasTimeLeft()) {
      this.nextChildren()
    }
  }

  def tryIterateForward(i: Int): Boolean = {

    var success = false
    val iter = this.iterators(i)

    // TODO This is a bit hacky.
    // It can happen if there is more than one child,
    // and one of the earlier child iterators is empty.
    if (iter == null) return false

    while (!success && iter.iterate() && this.deadline.hasTimeLeft()) {
      if (i == this.childCount - 1) {
        success = true
      } else {
        val (_, pre, post) = iter.getCurrent.get
        if (this.resetItersForward(i + 1, Some(pre), Some(post))) {
          success = this.iterators.last.iterate()
        }
      }
    }

    success
  }

  def resetItersForward(
      i: Int,
      pre: Option[PreCondition],
      post: Option[PostCondition]
  ): Boolean = {
    require(i >= 0)
    require(i < this.childCount)

    val typ = vocab.childTypes(i)
    val iter = new ChildIterator(this.makeProgramIter(post, typ), pre, post)
    this.iterators(i) = iter

    if (this.childCount <= i + 1) {
      return true
    } else if (iter.getCurrent.isEmpty && !iter.iterate()) {
      return false
    }

    // Peek the value of this iterator to build the next one!
    var (_, newPre, newPost) = iter.getCurrent.get

    while (!resetItersForward(i + 1, Some(newPre), Some(newPost))) {
      if (!iter.iterate() || this.deadline.isOverdue()) {
        return false
      }

      val (_, pr, po) = iter.getCurrent.get
      newPre = pr
      newPost = po
    }

    true
  }

  def makeProgramIter(postCondition: Option[PostCondition], typ: Types.Type): Iterator[Program[_]] = {
    this.previousLevelChildren.flatMap { case (preCondition: Condition, programs: mutable.Buffer[Program[_]]) =>
      if (postCondition.isEmpty || preCondition.maybeSatisfiableBy(postCondition.get)) {
        programs.filter(_.typ == typ)
      } else {
        Nil
      }
    }.iterator
  }
}
