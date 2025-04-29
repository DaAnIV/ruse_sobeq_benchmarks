package enumeration

import operations.{LiteralOperation, Operation, VariableOperation}
import task.SynthesisTask
import utils.Condition
import utils.Condition._
import vocab.Vocabulary

import scala.concurrent.duration.{Deadline, DurationInt}

class Enumerator(
    val vocab: Vocabulary,
    val task: SynthesisTask,
    val deadline: Deadline = 5.seconds.fromNow
) extends Iterator[Program[_]] {
  val ctxs: Int = task.examples.length
  var nextProgram: Option[Program[_]] = None

  // Setup the global variables
  // -------------------------------------------------------------------
  // The initial state. Must be set at the start of enumeration.
  Condition.initialState = task.initialState
  // -------------------------------------------------------------------

  // Setup the store
  // -------------------------------------------------------------------
  // First the variables
  val store: Store = Store.build()
  task.varMap.zipWithIndex.foreach { case (name, idx) =>
    val values = task.examples.map(_.inputs(name))
    val typ = task.variables(name)
    val variableProgram = VariableOperation.makeVariable(name, idx, typ, values, task.varMap.length)
    this.store.insert(variableProgram)
  }
  // Then the literals
  vocab.leaves.foreach { case op: LiteralOperation =>
    val program = op(task.initialState)
    this.store.insert(program)
  }
  // -------------------------------------------------------------------

  // The iterator hub
  // -------------------------------------------------------------------
  var vocabIter: Iterator[Operation] = this.vocab.operations.iterator
  var currVocab: Operation = this.vocabIter.next()
  var currLevelChildren = this.store.currLevel()
  var prevLevelChildrenIndex = -1
  var childrenIter =
    new ChildrenIterator(this.currLevelChildren, this.currVocab, deadline, this.prevLevelChildrenIndex, this.task.varMap.length)
  // -------------------------------------------------------------------

  override def hasNext: Boolean = {
    if (this.nextProgram.isEmpty) {
      this.nextProgram = this.getNextProgram()
    }
    this.nextProgram.isDefined
  }

  override def next(): Program[_] = {
    if (this.nextProgram.isEmpty) this.hasNext
    val rs = this.nextProgram.get
    this.nextProgram = None
    rs
  }

  def getNextProgram(): Option[Program[_]] = {
    var rs: Option[Program[_]] = None

    while (rs.isEmpty) {
      while (!childrenIter.hasNext) {
        // we need to return here, otherwise the call to childrenIter.next() below will throw an exception.
        if (this.deadline.isOverdue()) return rs

        // Move to the next vocab
        if (!vocabIter.hasNext) {
          // Go back to the first component
          this.vocabIter = this.vocab.operations.iterator
          // And move to the next level!
          this.currLevelChildren = this.store.currLevel()
        }
        this.currVocab = this.vocabIter.next()
        this.childrenIter = new ChildrenIterator(
          this.currLevelChildren,
          this.currVocab,
          this.deadline,
          this.prevLevelChildrenIndex,
          this.task.varMap.length
        )
      }

      // Build a new program!
      val (children, pre, post) = this.childrenIter.next()
      val program = this.currVocab(children, pre, post)
      val effects: Effect = program.effects()

      // First, we need to check if we mutated something we aren't allowed to.
      if (this.task.effectsAllowed(effects)) {
        if (this.store.insert(program)) {
          rs = Some(program)

          // Then add the effect variables to the store.
          effects.zipWithIndex.filter(_._1.isDefined).foreach { case (maybeVals, varIdx) =>
            val values = maybeVals.get
            val cond = Condition.initialState // emp(this.task.varMap.length).mutate(varIdx, values)
            val varname = task.varMap(varIdx)

            if (!store.containsPreCond(cond)) {
              val typ = this.task.variables(varname)
              val varProgram = VariableOperation.makeVariable(varname, varIdx, typ, values, task.varMap.length)
              val added = store.insert(varProgram)
            }
          }
        }
      }
    }

    rs
  }

}
