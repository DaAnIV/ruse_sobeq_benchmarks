package enumeration

import operations.Types
import utils.Condition
import utils.Condition._

import scala.collection.mutable

object Store {
  type Index = Int
  type Observer = (Types.Type, Seq[Any], Program.Address, PreCondition, PostCondition)
  type Ordering = (Types.Type, Seq[Any], Program.Address, Effect)

  trait Element {}
  case class ProgElem(program: Program[_]) extends Element {}
  case class RefElem(index: Index) extends Element {}

  trait StoreType {}
  case class SObEq() extends StoreType {
    override def toString(): String = "SObEq"
  }
  case class PureOE() extends StoreType {
    override def toString(): String = "Pure OE"

  }
  case class ClassicalOE() extends StoreType {
    override def toString(): String = "Classical"

  }
  case class NoOE() extends StoreType {
    override def toString(): String = "No-OE"

  }
  case class Subsumption() extends StoreType {
    override def toString(): String = "Subsume"
  }

  // Configurations!
  var storeType: StoreType = ClassicalOE()

  def build(): Store = {
    Store.storeType match {
      case SObEq()  => new SObEqStore()
      case PureOE() => new PureOEStore()
      case NoOE()   => new NoOEStore()
      case ClassicalOE() => new ClassicalOEStore()
      case Subsumption() => new SubsumptionStore()
      case other    => throw new IllegalArgumentException(f"Store type not supported: $other")
    }
  }
}

trait Store {
  import Store._

  var iteration: Int = 0
  val prevLevelMap: mutable.Map[PreCondition, mutable.Buffer[Program[_]]] = new mutable.HashMap()
  var currLevelMap: mutable.Map[PreCondition, mutable.Buffer[Program[_]]] = new mutable.HashMap()

  def insert(program: Program[_]): Boolean

  def subsumedByDiscarding: Int = 0

  def prunedByOE: Int

  def currLevel(): Iterable[(Condition, mutable.Buffer[Program[_]])] = {
    this.iteration += 1

    // Merge the currLevel into prevLevel
    this.currLevelMap.foreach { case (cond, progs) =>
      this.prevLevelMap
        .getOrElseUpdate(cond, new mutable.ListBuffer())
        .appendAll(progs)
    }

    // Start a new level
    this.currLevelMap = new mutable.HashMap()

    // Return an iterator over prevLevel
    this.prevLevelMap
  }

  def containsPreCond(cond: PreCondition): Boolean = this.prevLevelMap.contains(cond) || this.currLevelMap.contains(cond)

  def size: Int = this.prevLevelMap.map(_._2.length).sum + this.currLevelMap.map(_._2.length).sum

  @inline
  def buildObserver(program: Program[_]): Observer =
    (program.typ, program.values, program.address, program.preCondition, program.postCondition)
}

class SObEqStore extends Store {
  import Store._

  protected val oe: mutable.Set[(Types.Type, Seq[Any], Program.Address, PreCondition, PostCondition)] = new mutable.HashSet()
  protected var oed: Int = 0

  def prunedByOE: Int = this.oed

  def insert(program: Program[_]): Boolean = {
    val oeKey = this.buildObserver(program)
    if (!this.oe.add(oeKey)) {
      this.oed += 1
      return false
    }

    // Add this program to the store
    this.currLevelMap
      .getOrElseUpdate(program.preCondition, new mutable.ListBuffer())
      .append(program)
    true
  }
}

class PureOEStore extends Store {
  protected val oe: mutable.Set[(Types.Type, Seq[Any], Program.Address)] = new mutable.HashSet()
  protected var oed: Int = 0

  override def prunedByOE: Int = this.oed

  override def insert(program: Program[_]): Boolean = {
    // if force or program is non-pure, we always add
    // otherwise, check OE
    if (program.effects().exists(_.isEmpty)) {
      // Check OE!
      val observer = (program.typ, program.values, program.address)
      if (!this.oe.add(observer)) {
        this.oed += 1
        return false
      } 
    }

    // We are adding this program!
    this.currLevelMap.getOrElseUpdate(program.preCondition, new mutable.ListBuffer()).append(program)
    true
  }
}

class ClassicalOEStore extends Store {
  protected val oe: mutable.Set[(Types.Type, Seq[Any], Program.Address)] = new mutable.HashSet()
  protected var oed: Int = 0

  override def prunedByOE: Int = this.oed

  override def insert(program: Program[_]): Boolean = {
    // if force or program is non-pure, we always add
    // otherwise, check OE
    // if (program.effects().exists(_.isEmpty)) {
      // Check OE!
      val observer = (program.typ, program.values, program.address)
      if (!this.oe.add(observer)) {
        this.oed += 1
        return false
      } 
    // }

    // We are adding this program!
    this.currLevelMap.getOrElseUpdate(program.preCondition, new mutable.ListBuffer()).append(program)
    true
  }
}

class NoOEStore extends Store {

  override def prunedByOE: Int = 0

  override def insert(program: Program[_]): Boolean = {
    // We are adding *all* program!
    this.currLevelMap.getOrElseUpdate(program.preCondition, new mutable.ListBuffer()).append(program)
    true
  }
}

class SubsumptionStore extends Store {
  val ordering: mutable.Map[Store.Ordering, mutable.Buffer[Program[_]]] = new mutable.HashMap()
  protected val oe: mutable.Set[(Types.Type, Seq[Any], Program.Address, PreCondition, PostCondition)] = new mutable.HashSet()
  protected var oed: Int = 0
  protected var subsumed: Int = 0

  override def subsumedByDiscarding: Int = this.subsumed

  override def prunedByOE: Int = this.oed

  @inline
  def buildOrdering(program: Program[_]): Store.Ordering =
    (program.typ, program.values, program.address, Condition.effect(program.preCondition, program.postCondition))

  override def insert(program: Program[_]): Boolean = {
    val oeKey = this.buildObserver(program)
    if (!this.oe.add(oeKey)) {
      this.oed += 1
      return false
    }

    // See if
    // 1. There's a more general program
    // 2. This is more general than an existing program
    val orderingKey = this.buildOrdering(program)
    val orderingBuffer = this.ordering.getOrElseUpdate(orderingKey, new mutable.ListBuffer())

    if (orderingBuffer.exists(other => program.preCondition.implies(other.preCondition))) {
      // More general programs exists. So we don't add this to the store
      this.subsumed += 1
      return false
    }
    
    // if we get here, then we're adding this to the store
    // we're also guaranteed that this key exists in the ordering
    orderingBuffer.append(program)

    // Add this program to the store
    this.currLevelMap
      .getOrElseUpdate(program.preCondition, new mutable.ListBuffer())
      .append(program)

    true
  }
}