package task

import operations.Types
import operations.Types.Type
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.mozilla.javascript.ast._
import task.Format.formats

import java.io.FileInputStream
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import enumeration.Program
import utils.Condition._

case class Example(
    inputs: Map[String, Any],
    output: Any 
)

case class SynthesisTask(
    variables: Map[String, Type],
    stringLiterals: List[String],
    intLiterals: List[Int],
    returnType: Option[Type],
    immutable: Set[String],
    examples: List[Example],
    statePred: Option[List[Map[String, Any]]],
    solutions: List[String]
) {
  val outputs: List[Any] = examples.map(_.output)
  val varMap: List[String] = variables.keySet.toList
  val initialState: PreCondition = varMap.map(name => Some(examples.map(ex => ex.inputs(name)))).toCondition
  val endState: Option[PostCondition] = statePred.map(pred => {
    varMap.map(name => {
      val first = pred(0)
      if (first.contains(name)) {
        Some(pred.map(p => p(name)))
      } else {
        None
      }
    }).toCondition
  })
  val mutableVars: List[Boolean] = varMap.map(name => !immutable.contains(name)).toList

  def effectsAllowed(effects: Effect) = effects.zipWithIndex.forall {
    case (vals, idx) if vals.isDefined => this.mutableVars(idx)
    case _ => true
  }

  def apply(program: Program[_]): Boolean = {
    var rs = this.returnType match {
      case Some(returnType) => returnType == program.typ && program.values == this.outputs
      case None             => true
    }

    if (rs) {
      rs = this.initialState.implies(program.preCondition)
    }

    if (rs && this.statePred.isDefined) {
      rs = program.postCondition.implies(this.endState.get)

      // rs = this.statePred.get.zipWithIndex.forall { case (state, i) =>
      //   state.forall { case (varname, value) =>
      //     program.postCondition.contains(varname) && program.postCondition(varname)(i) == value
      //   }
      // }
    }

    rs
  }
}

object SynthesisTask {

  import org.mozilla.javascript._

  def valueFromString(value: String, typ: Type): Any = typ match {
    case Types.Int => value.toInt
    case operations.Types.IntArray =>
      val parser = new Parser()
      val parseTree = parser.parse(value, "", 0)
      val expr: ArrayLiteral = parseTree.getFirstChild.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[ArrayLiteral]
      expr.getElements
        .map {
          case literal: NumberLiteral => literal.getValue.toInt
          case e => -e.asInstanceOf[UnaryExpression].getOperand.asInstanceOf[NumberLiteral].getValue.toInt
        }
        .toList
    case Types.Bool => value.toBoolean
    case Types.String =>
      val parser = new Parser()
      val parseTree = parser.parse(value, "", 0)
      parseTree.getFirstChild.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[StringLiteral].getValue(false)
    case operations.Types.StringArray =>
      val parser = new Parser()
      val parseTree = parser.parse(value, "", 0)
      val expr: ArrayLiteral = parseTree.getFirstChild.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[ArrayLiteral]
      expr.getElements.map(e => e.asInstanceOf[StringLiteral].getValue(false)).toList
    case operations.Types.IntSet =>
      val parser = new Parser()
      val parseTree = parser.parse(value, "", 0)
      parseSet(parseTree).map(_.asInstanceOf[NumberLiteral].getValue.toInt).toSet
    case operations.Types.StringSet =>
      val parser = new Parser()
      val parseTree = parser.parse(value, "", 0)
      parseSet(parseTree).map(_.asInstanceOf[StringLiteral].getValue).toSet
  }

  def parseSet(n: Node): List[Node] = {
    n match {
      case null                                => Nil
      case _: NumberLiteral | _: StringLiteral => List(n)
      case n: InfixExpression                  => parseSet(n.getLeft) ++ parseSet(n.getRight)
      case n: ExpressionStatement              => parseSet(n.getExpression)
      case _: AstRoot | _: Scope               => parseSet(n.getFirstChild)
    }
  }

  def contextFromJson(jsonContext: Map[String, String], inputType: Map[String, Type]): Map[String, Any] = {
    jsonContext.map { case (varName, varValue) =>
      varName -> valueFromString(varValue, inputType(varName))
    }
  }

  def fromJson(jsonExample: JsonExample, inputType: Map[String, Type], outputType: Option[Type]): Example = {
    assert(jsonExample.input.keySet == inputType.keySet)
    assert(jsonExample.output.isEmpty || outputType.isDefined, "Output value is given, but output type not specified.")

    val input = contextFromJson(jsonExample.input, inputType)
    val output = jsonExample.output.map(valueFromString(_, outputType.get)).orNull

    Example(input, output)
  }

  def typeFromString(t: String): Type = t match {
    case "Int"      => Types.Int
    case "[Int]"    => Types.IntArray
    case "Bool"     => Types.Bool
    case "String"   => Types.String
    case "[String]" => Types.StringArray
    case "{Int}"    => Types.IntSet
    case "{String}" => Types.StringSet
    case _          => throw new IllegalArgumentException(s"Type `$t` not supported")
  }

  def fromJson(jsonSynthesisTask: JsonSynthesisTask): SynthesisTask = {
    val returnType = jsonSynthesisTask.returnType.map(typeFromString)
    val varTypes = jsonSynthesisTask.variables.map { case (name, strtype) => name -> typeFromString(strtype) }
    val examples: List[Example] = jsonSynthesisTask.examples.map(fromJson(_, varTypes, returnType))
    val immut: Set[String] = jsonSynthesisTask.immutable.map(_.toSet).getOrElse(Set.empty)
    val statePred: Option[List[Map[String, Any]]] = if (jsonSynthesisTask.examples.exists(_.state.isDefined)) {
      assert(jsonSynthesisTask.examples.forall(_.state.isDefined), f"Partial state predicates currently not supported.")
      Some(jsonSynthesisTask.examples.map(ex => contextFromJson(ex.state.get, varTypes)))
    } else {
      None
    }

    // Sanity check
    for (varName <- immut) {
      assert(varTypes.contains(varName), s"Type of `$varName` not found.")
    }

    SynthesisTask(
      varTypes,
      jsonSynthesisTask.stringLiterals,
      jsonSynthesisTask.intLiterals,
      returnType,
      immut,
      examples,
      statePred,
      jsonSynthesisTask.solution
    )
  }

  def fromFile(fileName: String): SynthesisTask = {
    val file = new FileInputStream(fileName)
    val jsonTask = parse(file).extract[JsonSynthesisTask]
    fromJson(jsonTask)
  }
}
