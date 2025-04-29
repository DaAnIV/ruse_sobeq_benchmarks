package context

import utils.Condition
import utils.Condition._

trait ContextDigger {
  def get(ctx: Condition): Seq[Any]
  def update(ctx: PreCondition, newVal: Seq[Any] => Seq[Any], retVal: Seq[Any] => Seq[Any]): (Seq[Any], PostCondition)
  val xpath: String
}

trait ArrayDigger extends ContextDigger {
  override def get(ctx: Condition): Seq[List[Any]]
}

trait SetDigger[T] extends ContextDigger {
  override def get(ctx: Condition): Seq[Set[T]]
}

trait VarDigger[T] extends ContextDigger {
  val varname: String
  val varIdx: Int
  val xpath: String = varname

  override def get(ctx: Condition): Seq[T] = ctx(varIdx).get.asInstanceOf[List[T]]

  override def update(
      preCondition: PreCondition,
      newVal: Seq[Any] => Seq[Any],
      retVal: Seq[Any] => Seq[Any]
  ): (Seq[Any], PostCondition) = {
    val currentVals = preCondition(this.varIdx).get
    val returnValues = retVal(currentVals)
    val mutatedValues = newVal(currentVals)
    val postCondition = preCondition // preCondition.mutate(this.varIdx, mutatedValues)
    (returnValues, postCondition)
  }
}

class IntVarDigger(val varname: String, val varIdx: Int) extends VarDigger[Int]
class StringVarDigger(val varname: String, val varIdx: Int) extends VarDigger[String]
class BoolVarDigger(val varname: String, val varIdx: Int) extends VarDigger[Boolean]
class IntArrayVarDigger(val varname: String, val varIdx: Int) extends VarDigger[List[Int]] with ArrayDigger
class StringArrayVarDigger(val varname: String, val varIdx: Int) extends VarDigger[List[String]] with ArrayDigger
class IntSetVarDigger(val varname: String, val varIdx: Int) extends VarDigger[Set[Int]] with SetDigger[Int]
class StringSetVarDigger(val varname: String, val varIdx: Int) extends VarDigger[Set[String]] with SetDigger[String]

class ArrayDereferenceDigger(val arr: ArrayDigger, val idxs: Seq[Int]) extends ContextDigger {
  override def get(ctx: Condition): Seq[Any] = arr.get(ctx).zip(idxs).map { case (l, i) => l(i) }

  override def update(ctx: PreCondition, newVal: Seq[Any] => Seq[Any], retVal: Seq[Any] => Seq[Any]): (List[Any], PostCondition) = {
    val values = get(ctx)
    val (_, postCondition) = arr.update(
      ctx,
      als => {
        val ls = als.asInstanceOf[List[List[Any]]]
        val values = ls.zip(idxs).map { case (l, i) => l(i) }
        val newValues = newVal(values).asInstanceOf[List[Any]]
        ls.zip(idxs).zip(newValues).map { case ((l, i), v) => l.updated(i, v) }
      },
      identity
    )
    (retVal(values).asInstanceOf[List[Any]], postCondition)
  }

  override val xpath: String = {
    idxs.addString(new StringBuilder(arr.xpath), "$", ",", "").toString()
  }
}
