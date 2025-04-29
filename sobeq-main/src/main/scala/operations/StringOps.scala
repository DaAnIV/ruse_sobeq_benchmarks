package operations

import enumeration.Program
import enumeration.Store._
import operations.Types.Type
import utils.Condition._

import java.util.regex.Pattern

class StringToUpper extends StringOperation with MethodCall with ReturnsBottom {
  override protected val name: String = "toUpperCase"

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[String] = {
    val values = children(0).values.map { case s: String => s.toUpperCase }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.String)
}

class StringToLower extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[String] = {
    val values = children(0).values.map { case s: String => s.toLowerCase }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "toLowerCase"

  override val childTypes: List[Type] = List(Types.String)
}

class StringUnarySlice extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val arg0 = children.head.values.asInstanceOf[List[String]]
    val arg1 = children(1).values.asInstanceOf[List[Int]]
    val values = arg0.zip(arg1).map { case (str, from) =>
      ArraySlice.slice(str.toList, from, str.length).mkString
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

  override protected val name: String = "slice"

  override val childTypes: List[Type] = List(Types.String, Types.Int)
}

class StringSlice extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val arg0 = children.head.values.asInstanceOf[List[String]]
    val arg1 = children(1).values.asInstanceOf[List[Int]]
    val arg2 = children(2).values.asInstanceOf[List[Int]]
    val values = arg0.zip(arg1).zip(arg2).map { case ((str, from), to) =>
      ArraySlice.slice(str.toList, from, to).mkString
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

  override protected val name: String = "slice"

  override val childTypes: List[Type] = List(Types.String, Types.Int, Types.Int)
}

//concat
class StringConcat extends StringOperation with ReturnsBottom {

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val lhs = children(0).values.asInstanceOf[List[String]]
    val rhs = children(1).values.asInstanceOf[List[String]]
    val values = lhs.zip(rhs).map { case (l, r) => l + r }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override val childTypes: List[Type] = List(Types.String, Types.String)

  override def code(childrenCode: Seq[(String, Boolean)]): String = childrenCode.map(parensIfNeeded).mkString(" + ")

  override protected val parenless: Boolean = false
}

//includes
class StringIncludes extends BoolOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val arg0 = children(0).values.asInstanceOf[List[String]]
    val arg1 = children(1).values.asInstanceOf[List[String]]
    val values = arg0.zip(arg1).map { case (s, w) => s.contains(w) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "includes"

  override val childTypes: List[Type] = List(Types.String, Types.String)

}

class StringLength extends IntOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] =
    Program(
      this,
      children.head.values.map { case s: String => s.length },
      children,
      pre,
      post,
      None
    )

  override val childTypes: List[Type] = List(Types.String)

  override def code(childrenCode: Seq[(String, Boolean)]): String = s"${parensIfNeeded(childrenCode.head)}.length"

  override protected val parenless: Boolean = true
}

//split
class StringSplit extends ListOperation[String] with MethodCall with ReturnsBottom {
  override protected val parenless: Boolean = true

  override protected val name: String = "split"

  override val childTypes: List[Type] = List(Types.String, Types.String)

  override def resultType: Type = Types.StringArray

  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val delim = children(1).values.asInstanceOf[List[String]]
    val values = str.zip(delim).map {
      case (str, "") => str.toCharArray.map(_.toString).toList
      case (str, delim) =>
        var rs: List[String] = List()
        var buff = str

        while (buff != null) {
          val idx = buff.indexOf(delim)

          if (idx == -1) {
            rs ::= buff
            buff = null
          } else {
            rs ::= buff.slice(0, idx)
            buff = buff.slice(idx + delim.length, buff.length)
          }
        }
        rs.reverse

      // TODO This alternative implementation is hackier, but potentially faster?
      //      case (str, "") => str.toCharArray.map(_.toString).toList
      //      case (str, delim) if str == delim => List("", "")
      //      case (str, delim) if !str.endsWith(delim) => str.split(Pattern.quote(delim)).toList
      //      case (str, delim) =>
      //        var rs = str.split(Pattern.quote(delim)).toList
      //        if (rs.mkString(delim) != str) rs = rs ++ List("")
      //        rs
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
}

//trim
class StringTrim extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[String] =
    Program(
      this,
      children.head.values.asInstanceOf[List[String]].map(_.trim),
      children,
      pre,
      post,
      None
    )

  override protected val name: String = "trim"

  override val childTypes: List[Type] = Types.String :: Nil

}

//replace
class StringReplace extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val arg0 = children(1).values.asInstanceOf[List[String]]
    val arg1 = children(2).values.asInstanceOf[List[String]]
    val values = str.zip(arg0.zip(arg1)).map { case (s, (a0, a1)) => s.replaceFirst(Pattern.quote(a0), a1) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "replace"

  override val childTypes: List[Type] = List(Types.String, Types.String, Types.String)

}

//replaceAll
class StringReplaceAll extends StringOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[String] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val arg0 = children(1).values.asInstanceOf[List[String]]
    val arg1 = children(2).values.asInstanceOf[List[String]]
    val values = str.zip(arg0.zip(arg1)).map { case (s, (a0, a1)) => s.replace(a0, a1) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "replaceAll"

  override val childTypes: List[Type] = List(Types.String, Types.String, Types.String)
}

//indexOf
class StringIndexOf extends IntOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val arg0 = children(1).values.asInstanceOf[List[String]]
    val values = str.zip(arg0).map { case (s, a) => s.indexOf(a) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "indexOf"

  override val childTypes: List[Type] = List(Types.String, Types.String)

}

//lastIndexOf
class StringLastIndexOf extends IntOperation with MethodCall with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[Int] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val arg0 = children(1).values.asInstanceOf[List[String]]
    val values = str.zip(arg0).map { case (s, a) => s.lastIndexOf(a) }
    Program(
      this,
      values,
      children,
      pre,
      post,
      None
    )
  }

  override protected val name: String = "lastIndexOf"

  override val childTypes: List[Type] = List(Types.String, Types.String)
}

class StringDereference extends StringOperation with ReturnsBottom {
  override def apply(children: Seq[Program[_]], pre: PreCondition, post: PostCondition): Program[_] = {
    val str = children.head.values.asInstanceOf[List[String]]
    val index = children(1).values.asInstanceOf[List[Int]]
    val values: List[String] = str.zip(index).map { case (s, i) => s.charAt(i).toString }
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
    children(0).values.zip(children(1).values).forall {
      case (str: String, i: Int) => i >= 0 && i < str.length
      case _                     => false
    }

  override val childTypes: List[Type] = List(Types.String, Types.Int)

  override def code(childrenCode: Seq[(String, Boolean)]): String =
    s"${parensIfNeeded(childrenCode.head)}[${childrenCode(1)._1}]"

  override protected val parenless: Boolean = true
}

//repeat
//startsWith
//endsWith
//dereference/charAt
//substring
