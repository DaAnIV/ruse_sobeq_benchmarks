package utils

import enumeration.{Program, Store}
import operations._

object PostProcessor {

  def process(node: Program[_], store: Store): Program[_] = {
    val subsumed = subsumptionPass(node, store)
    val constantFolded = constantFold(subsumed)
    sequenceFold(constantFolded)
  }

  def subsumptionPass(node: Program[_], store: Store): Program[_] = node
  // {
  //   // Recursively, top-down, check if any child nodes are subsumed by a more general program in the store
  //   val newChildren: Seq[Program[_]] = node.children.map(child => {
  //     val childEffects = child.effects()
  //     if (child.preCondition.isEmpty) {
  //       // This is as general as it gets
  //       child
  //     } else {
  //       // For all subsets of the precondition,
  //       // find programs with that subset as their precondition,
  //       // filter by the "ordering" (type, address, values and effect)
  //       // return the first one, or recurse for the child node's children
  //       child.preCondition
  //         .subsets()
  //         .flatMap(subset => store.prevLevelMap.getOrElse(subset, Nil) ++ store.currLevelMap.getOrElse(subset, Nil))
  //         .find(prog =>
  //           prog.typ == child.typ &&
  //             prog.address == child.address &&
  //             prog.values == child.values &&
  //             prog.effects() == childEffects
  //         )
  //         .getOrElse(subsumptionPass(child, store))
  //     }
  //   })

  //   val rs = Program(node.op, node.values, newChildren, node.preCondition, node.postCondition, node.digger)
  //   if (rs == node) {
  //     // we're hit the fix-point
  //     node
  //   } else {
  //     // Otherwise, another round!
  //     subsumptionPass(rs, store)
  //   }
  // }

  def sequenceFold(node: Program[_]): Program[_] = node.op match {
    case _: Sequence =>
      val listOfStatements: List[Program[_]] = flattenSequences(node)
      val allButLast = listOfStatements.slice(0, listOfStatements.length - 1).filter(c => c.preCondition != c.postCondition).toList
      val last = listOfStatements.last
      allButLast match {
        case Nil => last
        case (head :: Nil) => Program(new Sequence { override val childTypes: List[Types.Type] = List(head.typ, last.typ) }, node.values, List(head, last), node.preCondition, node.postCondition, node.digger)
        case _ => Program(new InfiniteSequence(), last.values, allButLast :+ last, node.preCondition, node.postCondition, None)
      }
    case _ => node
  }

  def flattenSequences(node: Program[_]): List[Program[_]] = node.op match {
    case _: Sequence => node.children.flatMap(flattenSequences(_)).toList
    case _           => node :: Nil
  }

  def constantFold(node: Program[_]): Program[_] = node.op match {
    case _: IntAddition =>
      val lhs = constantFold(node.children.head)
      val rhs = constantFold(node.children(1))
      (lhs.op, rhs.op) match {
        case (_: IntLiteral, _: IntLiteral) => intAdditionFold(node, lhs, rhs)
        case (_: IntAddition, _: IntLiteral) if lhs.children(1).op.isInstanceOf[IntLiteral] =>
          val newLhs = lhs.children.head
          val newRhs = intAdditionFold(node, lhs.children(1), rhs)
          Program(new IntAddition, node.values, newLhs :: newRhs :: Nil, rhs.preCondition, rhs.postCondition, None)
        case (_: IntLiteral, _: IntAddition) if rhs.children.head.op.isInstanceOf[IntLiteral] =>
          val newLhs = intAdditionFold(node, lhs, rhs.children.head)
          val newRhs = rhs.children(1)
          Program(new IntAddition, node.values, newLhs :: newRhs :: Nil, lhs.preCondition, lhs.postCondition, None)
        case _ =>
          val Some((pre, post)) = Condition.makeValid((lhs.preCondition, lhs.postCondition), (rhs.preCondition, rhs.postCondition))
          Program(node.op, node.values, lhs :: rhs :: Nil, pre, post, None)
      }
    case _: IntSubtraction =>
      val lhs = constantFold(node.children.head)
      val rhs = constantFold(node.children(1))
      (lhs.op, rhs.op) match {
        case (_: IntLiteral, _: IntLiteral) => intSubtractionFold(node, lhs, rhs)
        case _ =>
          val Some((pre, post)) = Condition.makeValid((lhs.preCondition, lhs.postCondition), (rhs.preCondition, rhs.postCondition))
          Program(node.op, node.values, lhs :: rhs :: Nil, pre, post, None)
      }
    case _: IntUnaryMinus =>
      val child = constantFold(node.children.head)
      child.op match {
        case _: IntLiteral =>
          Program(
            new IntLiteral(-child.values.head.asInstanceOf[Int], child.values.length),
            node.values,
            Nil,
            child.preCondition,
            child.postCondition,
            None
          )
        case _ =>
          Program(node.op, node.values, child :: Nil, child.preCondition, child.postCondition, None)
      }
    case _: StringConcat =>
      val lhs = constantFold(node.children.head)
      val rhs = constantFold(node.children(1))
      (lhs.op, rhs.op) match {
        case (_: StringLiteral, _: StringLiteral) => strConcatFold(node, lhs, rhs)
        case (_: StringConcat, _: StringLiteral) if lhs.children.last.op.isInstanceOf[StringLiteral] =>
          val newLhs = lhs.children.head
          val newRhs = strConcatFold(node, lhs.children(1), rhs)
          val Some((pre, post)) =
            Condition.makeValid((newLhs.preCondition, newLhs.postCondition), (newRhs.preCondition, newRhs.postCondition))
          Program(new StringConcat, node.values, newLhs :: newRhs :: Nil, pre, post, None)
        case (_: StringLiteral, _: StringConcat) if rhs.children.head.op.isInstanceOf[StringLiteral] =>
          val newLhs = strConcatFold(node, lhs, rhs.children.head)
          val newRhs = rhs.children(1)
          val Some((pre, post)) =
            Condition.makeValid((newLhs.preCondition, newLhs.postCondition), (newRhs.preCondition, newRhs.postCondition))
          Program(new StringConcat, node.values, newLhs :: newRhs :: Nil, pre, post, None)
        case _ =>
          val Some((pre, post)) = Condition.makeValid((lhs.preCondition, lhs.postCondition), (rhs.preCondition, rhs.postCondition))
          Program(node.op, node.values, lhs :: rhs :: Nil, pre, post, None)
      }
    case _ =>
      Program(node.op, node.values, node.children.map(constantFold), node.preCondition, node.postCondition, node.digger)
  }

  def intAdditionFold(
      node: Program[_],
      lhs: Program[_],
      rhs: Program[_]
  ): Program[_] = {
    assert(lhs.op.isInstanceOf[IntLiteral])
    assert(rhs.op.isInstanceOf[IntLiteral])
    val value = lhs.values.head.asInstanceOf[Int] + rhs.values.head.asInstanceOf[Int]
    Program(new IntLiteral(value, lhs.values.length), node.values.map(_ => value), Nil, lhs.preCondition, lhs.postCondition, None)
  }

  def intSubtractionFold(
      node: Program[_],
      lhs: Program[_],
      rhs: Program[_]
  ): Program[_] = {
    assert(lhs.op.isInstanceOf[IntLiteral])
    assert(rhs.op.isInstanceOf[IntLiteral])
    val value = lhs.values.head.asInstanceOf[Int] - rhs.values.head.asInstanceOf[Int]
    Program(new IntLiteral(value, lhs.values.length), node.values.map(_ => value), Nil, lhs.preCondition, lhs.postCondition, None)
  }

  def strConcatFold(
      node: Program[_],
      lhs: Program[_],
      rhs: Program[_]
  ): Program[_] = {
    assert(lhs.op.isInstanceOf[StringLiteral])
    assert(rhs.op.isInstanceOf[StringLiteral])
    val value = lhs.values.head.asInstanceOf[String] + rhs.values.head.asInstanceOf[String]
    Program(new StringLiteral(value, lhs.values.length), node.values.map(_ => value), Nil, lhs.preCondition, lhs.postCondition, None)
  }
}
