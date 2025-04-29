package vocab

import operations.Types.Type
import operations._
import task.SynthesisTask

class Vocabulary(val leaves: List[LeafOperation], val operations: List[Operation])

object Vocabulary {
  def apply(operations: List[Operation], leafOperations: List[LeafOperation]) =
    new Vocabulary(leafOperations, operations)

  def vocabFromTask(task: SynthesisTask) = {
    val leaves = leavesFromTask(task)
    val ops = sequenceOperations ++
      nonMutatingOperations ++
      // assignmentOperations(task) ++
      mutatingOperations
    Vocabulary(ops, leaves)
  }

  def immutVocabFromTask(task: SynthesisTask) = {
    val leaves = leavesFromTask(task)
    val ops = nonMutatingOperations
    Vocabulary(ops, leaves)
  }

  private def leavesFromTask(task: SynthesisTask): List[LeafOperation] =
    // task.variables.map { case (name, t) =>
    //   t match {
    //     case operations.Types.Int         => new IntVariable(name)
    //     case operations.Types.IntArray    => new IntArrayVariable(name)
    //     case operations.Types.StringArray => new StringArrayVariable(name)
    //     case operations.Types.String      => new StringVariable(name)
    //     case operations.Types.Bool        => new BoolVariable(name)
    //     case typ                          => throw new IllegalArgumentException(s"Variables of type $typ are not supported.")
    //   }
    // }.toList ++
    List(
      new IntLiteral(0, task.examples.size),
      new IntLiteral(1, task.examples.length),
      new StringLiteral("", task.examples.length),
      new StringLiteral(" ", task.examples.length),
      new IntArrayLiteral(List(), task.examples.length),
      new StringArrayLiteral(Nil, task.examples.length)
    ) ++
      task.stringLiterals.map(new StringLiteral(_, task.examples.length)) ++
      task.intLiterals.map(new IntLiteral(_, task.examples.length))

  private def nonMutatingOperations: List[Operation] = List(
    // Int
    new IntUnaryMinus,
    new IntAddition,
    new IntSubtraction,
    // new IntMultiplication,
    // new IntPowerOperation,
    // new IntDivision,
    // new IntMod,
    // String
    new StringDereference,
    new StringLength,
    new StringEq,
    new StringToUpper,
    // new StringToLower,
    new StringSplit,
    new StringTrim,
    new StringUnarySlice,
    new StringSlice,
    new StringConcat,
    // new StringIncludes,
    new StringReplace,
    new StringReplaceAll,
    new StringIndexOf,
    new StringLastIndexOf,
    // IntArrays
    new IntArrayDereference,
    new IntArrayConstructor,
    new IntArrayLength,
    new IntArraySlice2,
    new IntArraySlice,
    new IntArrayConcat,
    new IntArrayJoin,
    // StringArrays
    new StringArrayDereference,
    new StringArrayConstructor,
    new StringArrayLength,
    new StringArraySlice2,
    new StringArraySlice,
    new StringArrayConcat,
    new StringArrayJoin,
    // IntSets
    new IntSetAdd,
    new IntSetHas,
    new IntSetDelete,
    new IntSetSize,
    // StringSets
    new StringSetAdd,
    new StringSetHas,
    new StringSetDelete,
    new StringSetSize,
    // Booleans
    // new GreaterThanEq,
    new LessThan
    // new And
  )

  private def sequenceOperations: List[Operation] = {
    // Create all possible types of Sequence, using the cartesian product of the types
    val iter = for { first <- Types.values.iterator; rest <- Types.values.iterator } yield new Sequence {
      override val childTypes: List[Type] = List(first, rest)
    }
    iter.toList
  }

  // private def assignmentOperations(task: SynthesisTask): List[Operation] = task.variables
  //   .filter { case (n, _) => !task.immutable.contains(n) }
  //   .filter { case (_, t) => t == Types.Int || t == Types.Bool || t == Types.String }
  //   .map { case (varName, varType) => new Assignment(varName, varType) }
  //   .toList

  private def mutatingOperations: List[Operation] = List(
    // Int
    new IntIncrementPostfix,
    // IntArrays
    new IntArrayPush,
    new IntArraySort,
    new IntArrayReverse,
    new IntArraySplice,
    new IntArraySplice2,
    new IntArrayTernarySplice,
    new IntArrayPop,
    new IntArrayShift,
    // StringArrays
    new StringArrayPush,
    new StringArraySort,
    new StringArrayReverse,
    new StringArraySplice,
    new StringArraySplice2,
    // new StringArrayTernarySplice,
    new StringArrayPop,
    new StringArrayShift
  )
}
