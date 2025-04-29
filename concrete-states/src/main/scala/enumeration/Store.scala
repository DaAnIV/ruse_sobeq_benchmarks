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
  case class NoOE() extends StoreType {
    override def toString(): String = "No-OE"

  }

  // Configurations!
  var subsumeByReplacement: Boolean = false
  var subsumeByDiscarding: Boolean = true
  var storeType: StoreType = SObEq()

  def build(): Store = {
    Store.storeType match {
      case SObEq()  => new SObEqStore()
      case PureOE() => new PureOEStore()
      case NoOE()   => new NoOEStore()
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
    this.prevLevelMap.toList
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

  def insert(program: Program[_]): Boolean = {
    val oeKey = this.buildObserver(program)
    if (!this.oe.add(oeKey)) return false

    // Add this program to the store
    this.currLevelMap
      .getOrElseUpdate(program.preCondition, new mutable.ListBuffer())
      .append(program)
    true
  }
}

class PureOEStore extends Store {
  protected val oe: mutable.Set[(Types.Type, Seq[Any], Program.Address)] = new mutable.HashSet()

  override def insert(program: Program[_]): Boolean = {
    // if force or program is non-pure, we always add
    // otherwise, check OE
    if (program.effects().exists(_.isEmpty)) {
      // Check OE!
      val observer = (program.typ, program.values, program.address)
      if (!this.oe.add(observer)) {
        return false
      }
    }

    // We are adding this program!
    this.currLevelMap.getOrElseUpdate(program.preCondition, new mutable.ListBuffer()).append(program)
    true
  }
}

class NoOEStore extends Store {

  override def insert(program: Program[_]): Boolean = {
    // We are adding *all* program!
    this.currLevelMap.getOrElseUpdate(program.preCondition, new mutable.ListBuffer()).append(program)
    true
  }
}
