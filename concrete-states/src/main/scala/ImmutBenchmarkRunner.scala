import Main.doSynthesize
import enumeration.{ChildrenIterator, EnumerationOrderGenerator}
import task.SynthesisTask
import utils.{BenchmarkConfig, SynthesisResult}
import vocab.Vocabulary

import java.nio.file.Path

class ImmutBenchmarkRunner(override val config: BenchmarkConfig) extends BenchmarkRunner(config) {

  /**
    * For each valid benchmark, set the immut predicate to *everything* and run it twice:
    *   1. Once with the normal vocabulary, and 2. Once with the non-muting vocabulary which has the array `.slice(0)` wrapper. If the
    *      benchmark invalid (i.e. has a state predicate and thus requires mutation) we just return an empty List, since it doesn't apply to
    *      this RQ anyway.
    */
  override def runBenchmark(task: SynthesisTask, path: Path, timeout: Int, print: Boolean = true): List[SynthesisResult] = {
    if (task.statePred.isDefined) {
      List()
    } else {
      val newTask = new SynthesisTask(
        task.variables,
        task.stringLiterals,
        task.intLiterals,
        task.returnType,
        task.variables.keySet, // Set all variables to be immutable.
        task.examples,
        task.statePred,
        task.solutions
      )

      // First, run it with a non-mutating vocab
      val nonMutatingResult = doSynthesize(
        newTask,
        timeout,
        debug = false,
        bothSolutions = this.bothSolutions,
        vocabulary = Vocabulary.immutVocabFromTask(newTask)
      )
      // Sanity check:
      nonMutatingResult.filter(s => s.mut && s.solution.isDefined) match {
        case Nil => ()
        case mut @ _ :: _ =>
          System.err.println(
            s"Non-mutating result for `$path` mutates. This shouldn't happen: `${mut.map(_.solution.get).mkString("; ")}` "
          )
      }

      // Then, rerun it with our usual vocab
      val mutatingResult = doSynthesize(newTask, timeout, debug = false, bothSolutions = this.bothSolutions)
      // Sanity check:
      mutatingResult.filter(s => s.mut && s.solution.isDefined) match {
        case Nil => ()
        case mut @ _ :: _ =>
          System.err.println(
            s"Regular-grammar result for `$path` mutates. This shouldn't happen: `${mut.map(_.solution.get).mkString("; ")}` "
          )
      }

      nonMutatingResult.map(r =>
        SynthesisResult(
          mut = false,
          r.solution,
          r.time,
          r.storeSize,
          r.programsSubsumedByDiscarding,
          r.programsSubsumedByReplacement,
          r.storeType,
          r.assertions
        )
      ) ++
        mutatingResult.map(r =>
          SynthesisResult(
            mut = true,
            r.solution,
            r.time,
            r.storeSize,
            r.programsSubsumedByDiscarding,
            r.programsSubsumedByReplacement,
            r.storeType,
            r.assertions
          )
        )
    }
  }
}

object ImmutBenchmarkRunner extends App {
  val defaultConfigStr =
    """{
      |    "benchmark": "src/test/benchmarks/non-mutating",
      |    "timeout": 60,
      |    "orders": ["exp4"],
      |    "bothSolutions": false,
      |    "testSeen": false,
      |    "outputDirName": "benchmark_results_immut"
      |}""".stripMargin

  val config: BenchmarkConfig = if (args.isEmpty) {
    BenchmarkConfig.get(defaultConfigStr)
  } else {
    BenchmarkConfig.get(args.head)
  }
  val runner: BenchmarkRunner = new ImmutBenchmarkRunner(this.config)

  // First, warm up!
  println("Warming up Immut benchmarks. Please wait...")
  this.runner.runBenchmarks(this.runner.benchmarkPath, timeout = 5, print = false)
  System.gc()
  println("Warmup done. Starting benchmarks...")
  this.runner.runBenchmarks(this.runner.benchmarkPath, this.runner.timeout)
  this.runner.csvWriter.close()
}
