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
  this.leavesForPrecondition(task.initialState).foreach(p => {
    this.store.insert(p) 
  })
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

    
  def leavesForPrecondition(cond: Condition): List[Program[_]] = {
    // This build all the leaves for the given condition!
    // It uses the given vocab and task to do so.
    task.varMap.zipWithIndex.map{ case (name, index) => 
        val typ = task.variables(name)
        VariableOperation.makeVariable(name, index, typ, cond, task.varMap.length)
    }.toList ++
    vocab.leaves.map(op => {
      op(cond)
    })
  }

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

          if (!store.containsPreCond(program.postCondition)) {
            // New precondition!
            this.leavesForPrecondition(program.postCondition).foreach(p => {
              this.store.insert(p)
            })
          }
        }
      }
    }

    rs
  }
}
