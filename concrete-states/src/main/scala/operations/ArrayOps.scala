package operations
import context.{ArrayDereferenceDigger, ArrayDigger, ContextDigger}
import enumeration.Program
import enumeration.Store.Index
import operations.Types.{IntArray, StringArray, Type, elementOf}
import utils.Condition._

abstract class ArrayConstructor[T](t: Type) extends ListOperation[T] with ReturnsBottom {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) =
    Program(
      this,
      children(0).values.map(List(_)),
      children,
      pre,
      post,
      None
    )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(t)
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = "[" + childrenCode.head._1 + "]"
}

class IntArrayConstructor extends ArrayConstructor[Int](Types.Int) with ReturnsIntArray
class StringArrayConstructor extends ArrayConstructor[String](Types.String) with ReturnsStringArray

abstract class ArrayDereference[T](arrType: Type) extends Operation {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) =
    Program(
      this,
      children(0).currentValues(post).zip(children(1).values).map { case (a: List[_], i: Int) => a(i) },
      children,
      pre,
      post,
      this.makeDigger(children)
    )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = {
    val lhs = children.head.currentValues(cond).asInstanceOf[List[List[T]]]
    val rhs = children(1).values.asInstanceOf[List[Int]]
    lhs.zip(rhs).forall { case (arr, idx) => idx >= 0 && idx < arr.length }
  }

  override val childTypes: List[Type] = List(arrType, Types.Int)
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = parensIfNeeded(childrenCode.head) + "[" + childrenCode(1)._1 + "]"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] =
    children.head.digger.map(arrayDigger =>
      new ArrayDereferenceDigger(arrayDigger.asInstanceOf[ArrayDigger], children(1).values.asInstanceOf[Seq[Int]])
    )
}

class IntArrayDereference extends ArrayDereference[Int](Types.IntArray) with ReturnsInt
class StringArrayDereference extends ArrayDereference[String](Types.StringArray) with ReturnsString

abstract class ArrayLength[T](arrType: Type) extends IntOperation with ReturnsBottom {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) =
    Program(
      this,
      children(0).values.map { case arr: List[_] => arr.length },
      children,
      pre,
      post,
      None
    )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType)
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = parensIfNeeded(childrenCode.head) + ".length"
}

class IntArrayLength extends ArrayLength[Int](Types.IntArray)
class StringArrayLength extends ArrayLength[String](Types.StringArray)

abstract class ArraySlice[T](arrType: Type) extends ListOperation[T] with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) =
    Program(
      this,
      children(0).currentValues(post).zip(children(1).values).map { case (arr: List[_], idx: Int) =>
        ArraySlice.slice(arr, idx, arr.length)
      },
      children,
      pre,
      post,
      None
    )

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType, Types.Int)
  override protected val name: String = "slice"
}

abstract class ArraySlice2[T](arrType: Type) extends ListOperation[T] with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val values =
      children(0).currentValues(post).zip(children(1).values).zip(children(2).values).map { case ((arr: List[_], from: Int), until: Int) =>
        ArraySlice.slice(arr, from, until)
      }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }
  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType, Types.Int, Types.Int)
  override protected val name: String = "slice"
}

object ArraySlice {
  def slice[T](arr: List[T], from: Int, until: Int): List[T] = {
    val realFrom = if (from >= 0) from else arr.length + from
    val realUntil = if (until >= 0) until else arr.length + until
    arr.slice(realFrom, realUntil)
  }
}

class IntArraySlice extends ArraySlice[Int](Types.IntArray) with ReturnsIntArray
class StringArraySlice extends ArraySlice[String](Types.StringArray) with ReturnsStringArray
class IntArraySlice2 extends ArraySlice2[Int](Types.IntArray) with ReturnsIntArray
class StringArraySlice2 extends ArraySlice2[String](Types.StringArray) with ReturnsStringArray

abstract class ArraySort[T](arrType: Type, funcArg: String)(implicit ord: Ordering[T]) extends ListOperation[T] {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val child = children(0).asInstanceOf[Program[List[T]]]
    val sortFunc = (ls: Seq[Any]) => ls.asInstanceOf[Seq[List[T]]].map(_.sorted)
    val (values, postCondition) = (child.digger match {
      case Some(digger: ArrayDigger) => digger.update(post, sortFunc, sortFunc)
      case None                      => (sortFunc(child.values), post)
    }).asInstanceOf[(List[List[T]], PostCondition)]

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      child.digger
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType)

  // override protected val name: String = "sort"
  override protected val parenless: Boolean = true
  override def code(childrenCode: Seq[(String, Boolean)]): String = parensIfNeeded(childrenCode.head) + s".sort($funcArg)"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = children.head.digger // self reference
}

class IntArraySort extends ArraySort[Int](IntArray, "(n1,n2) => n1 - n2") with ReturnsIntArray
class StringArraySort extends ArraySort[String](StringArray, "") with ReturnsStringArray

abstract class ArrayReverse[T](arrType: Type) extends ListOperation[T] with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val child = children(0).asInstanceOf[Program[List[T]]]
    val reverseFunc = (ls: Seq[Any]) => ls.asInstanceOf[Seq[List[T]]].map(lst => lst.reverse)
    val (values, postCondition) = (child.digger match {
      case Some(digger) => digger.update(post, reverseFunc, reverseFunc)
      case None         => (reverseFunc(child.values), post)
    }).asInstanceOf[(List[List[T]], PostCondition)]

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      child.digger
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType)

  override protected val name: String = "reverse"
  // override def code(childrenCode: Seq[String]): String = childrenCode.head + ".reverse()"

  override def makeDigger(children: Seq[Program[_]]): Option[ContextDigger] = children.head.digger
}

class IntArrayReverse extends ArrayReverse[Int](IntArray) with ReturnsIntArray
class StringArrayReverse extends ArrayReverse[String](StringArray) with ReturnsStringArray

abstract class TernarySplice[T](elemType: Type) extends ListOperation[T] with ReturnsBottom {

  protected val parenless: Boolean = true

  override def code(childrenCode: Seq[(String, Boolean)]): String = {
    val obj = childrenCode.head
    val args = childrenCode.tail
    s"${parensIfNeeded(obj)}.splice(${args.take(args.length - 1).map(_._1).mkString(",")}, 1, ${args.last._1})"
  }

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    // arr.splice(inclusive start, 1, new element to add)

    // Get the arguments
    val lists: Seq[List[T]] = children(0).currentValues(post).asInstanceOf[Seq[List[T]]]
    val starts: Seq[Int] = children(1).values.asInstanceOf[Seq[Int]].zip(lists).map { case (start, list) =>
      var rs = start
      if (rs < 0) {
        rs = list.length + start
      }
      if (rs < 0) {
        rs = 0
      }
      rs
    }

    // val counts: Seq[Int] = children(2).values.asInstanceOf[Seq[Int]].map(c => if (c >= 0) c else 0)
    val newElements: Seq[T] = children(2).values.asInstanceOf[Seq[T]]

    // Lambda for the actual splice
    val newVal: Seq[Any] => Seq[Any] = (arr: Seq[Any]) =>
      arr.asInstanceOf[List[List[T]]].lazyZip(starts).lazyZip(newElements).map { case (list, start, elem) =>
        list.splitAt(start)._1 ++ List(elem) ++ list.splitAt(start + 1)._2
      }
    val retVal: Seq[Any] => Seq[Any] = (arr: Seq[Any]) =>
      arr.asInstanceOf[List[List[T]]].lazyZip(starts).map { case (list, start) =>
        list.splitAt(start)._2.splitAt(1)._1
      }

    val (values, postCondition) = children.head.digger match {
      case Some(digger) => digger.update(post, newVal, retVal)
      case None         => (retVal(lists), post)
    }

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.arrayOf(elemType), Types.Int, elemType)
}

class IntArrayTernarySplice extends TernarySplice[Int](Types.Int) with ReturnsIntArray
class StringArrayTernarySplice extends TernarySplice[String](Types.String) with ReturnsStringArray

abstract class ArraySplice[T](arrType: Type) extends ListOperation[T] with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val arr = children(0).asInstanceOf[Program[List[T]]]
    val index = children(1).asInstanceOf[Program[Int]]

    // TODO What are the correct conditions to use here?
    val lists = arr.currentValues(post)
    val starts = index.values.zip(lists).map {
      case (start, _) if start >= 0 => start
      case (start, list)            => list.length + start
    }

    val splitRes = (lists: Any) =>
      lists.asInstanceOf[List[List[T]]].zip(starts).map { case (list, start) =>
        list.splitAt(start)._2
      }

    val (values, postCondition) = (arr.digger match {
      case Some(digger) =>
        digger.update(
          post,
          lists => {
            lists.asInstanceOf[List[List[T]]].zip(starts).map { case (list, start) =>
              list.splitAt(start)._1
            }
          },
          splitRes
        )
      case None => (splitRes(lists), post)
    }).asInstanceOf[(List[List[T]], PostCondition)]

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      None
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType, Types.Int)

  // override def code(childrenCode: Seq[String]): String = childrenCode.head + ".splice(" + childrenCode(1) + ")"
  override protected val name: String = "splice"
}

class IntArraySplice extends ArraySplice[Int](IntArray) with ReturnsIntArray
class StringArraySplice extends ArraySplice[String](StringArray) with ReturnsStringArray

abstract class ArraySplice2[T](arrType: Type) extends ListOperation[T] with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    // arr.splice(inclusive start, count of elements to remove, new element to add)

    // Get the arguments
    val lists: Seq[List[T]] = children(0).currentValues(post).asInstanceOf[Seq[List[T]]]
    val starts: Seq[Int] = children(1).values.asInstanceOf[Seq[Int]].zip(lists).map { case (start, list) =>
      var rs = start
      if (rs < 0) {
        rs = list.length + start
      }
      if (rs < 0) {
        rs = 0
      }
      rs
    }
    val counts: Seq[Int] = children(2).values.asInstanceOf[Seq[Int]].map(c => if (c >= 0) c else 0)

    // Lambda for the actual splice
    val newVal: Seq[Any] => Seq[Any] = (arr: Seq[Any]) =>
      arr.asInstanceOf[List[List[T]]].lazyZip(starts).lazyZip(counts).map { case (list, start, count) =>
        list.splitAt(start)._1 ++ list.splitAt(start + count)._2
      }
    val retVal: Seq[Any] => Seq[Any] = (arr: Seq[Any]) =>
      arr.asInstanceOf[List[List[T]]].lazyZip(starts).lazyZip(counts).map { case (list, start, count) =>
        // list.splitAt(start)._2.splitAt(count)._1
        list.slice(start, start + count)
      }

    val (values, postCondition) = children.head.digger match {
      case Some(digger) => digger.update(post, newVal, retVal)
      case None         => (retVal(lists), post)
    }

    Program(
      this,
      values,
      children,
      pre,
      postCondition,
      None
    )
  }

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true

  override val childTypes: List[Type] = List(arrType, Types.Int, Types.Int)

  override protected val name: String = "splice"
}

class IntArraySplice2 extends ArraySplice2[Int](IntArray) with ReturnsIntArray
class StringArraySplice2 extends ArraySplice2[String](StringArray) with ReturnsStringArray

abstract class ArrayPush[T](arrType: Type, elemType: Type) extends IntOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val arr = children(0).asInstanceOf[Program[List[T]]]
    val elem = children(1).asInstanceOf[Program[T]]

    val (values, postCondition) = (arr.digger match {
      case Some(digger) =>
        digger.update(
          post,
          ls => ls.asInstanceOf[List[List[T]]].zip(elem.values).map { case (a, e) => a :+ e },
          ls => ls.asInstanceOf[List[List[T]]].map(_.length + 1)
        )
      case None =>
        (arr.values.map(_.length + 1), post)
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
    // TODO Should we only allow this for lists which are lhs?
    true

  override val childTypes: List[Type] = List(arrType, elemType)

  // override def code(childrenCode: Seq[String]): String = s"${childrenCode.head}.push(${childrenCode(1)})"
  override protected val name: String = "push"
}

class IntArrayPush extends ArrayPush[Int](IntArray, Types.Int)
class StringArrayPush extends ArrayPush[String](StringArray, Types.String)

abstract class ArrayPop[T](arrType: Type) extends Operation with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val child = children(0).asInstanceOf[Program[List[T]]]
    val (values, postCondition) = (child.digger match {
      case Some(digger) =>
        digger.update(
          post,
          ls => ls.asInstanceOf[List[List[T]]].map(a => a.slice(0, a.length - 1)),
          ls => ls.asInstanceOf[List[List[T]]].map(_.last)
        )
      case None => (child.values.map(_.last), post)
    }).asInstanceOf[(List[T], PostCondition)]

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
    children.head.currentValues(cond).forall(_.asInstanceOf[List[_]].nonEmpty)

  override val childTypes: List[Type] = List(arrType)

  override protected val name: String = "pop"
}

class IntArrayPop extends ArrayPop[Int](IntArray) with IntOperation with ReturnsBottom
class StringArrayPop extends ArrayPop[String](StringArray) with StringOperation with ReturnsBottom

abstract class ArrayShift[T](arrType: Type) extends Operation with MethodCall {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val child = children(0).asInstanceOf[Program[List[T]]]
    val shiftRes = (arr: Seq[Any]) => arr.asInstanceOf[Seq[List[T]]].map(_.head)
    val newArrs = (arr: Seq[Any]) => arr.asInstanceOf[Seq[List[T]]].map(_.drop(1))

    val (values, postCondition) = (child.digger match {
      case Some(digger) => digger.update(post, newArrs, shiftRes)
      case None         => (shiftRes(child.values), post)
    }).asInstanceOf[(List[T], PostCondition)]

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
    children.head.currentValues(cond).asInstanceOf[List[List[_]]].forall(!_.isEmpty)

  override val childTypes: List[Type] = List(arrType)

  override protected val name: String = "shift"
}

class IntArrayShift extends ArrayShift[Int](IntArray) with ReturnsInt with ReturnsBottom
class StringArrayShift extends ArrayShift[String](StringArray) with ReturnsString with ReturnsBottom

abstract class ArrayConcat[T](arrType: Type) extends ListOperation[T] with MethodCall {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val values = children(0).currentValues(post).zip(children(1).currentValues(post)).map { case (l: List[_], r: List[_]) => l.concat(r) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }
  override protected val name: String = "concat"
  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true
  override val childTypes: List[Type] = List(arrType, arrType)
}

class IntArrayConcat extends ArrayConcat[Int](IntArray) with ReturnsIntArray with ReturnsBottom
class StringArrayConcat extends ArrayConcat[String](StringArray) with ReturnsStringArray with ReturnsBottom

abstract class ArrayJoin[T](arrType: Type) extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition) = {
    val values = children(0).currentValues(post).zip(children(1).values).map { case (l: List[_], s: String) => l.mkString(s) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "join"

  override def _canApply(children: Seq[Program[_]], cond: PostCondition): Boolean = true
  override val childTypes: List[Type] = List(arrType, Types.String)
}

class IntArrayJoin extends ArrayJoin[Int](IntArray)
class StringArrayJoin extends ArrayJoin[String](StringArray)
