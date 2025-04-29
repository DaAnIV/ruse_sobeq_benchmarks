import Main.doSynthesize
import enumeration.{ChildrenIterator, EnumerationOrderGenerator}
import org.json4s.jackson.Serialization
import org.json4s.prefs.EmptyValueStrategy
import org.json4s.{DefaultFormats, Formats}
import task.SynthesisTask
import utils.{BenchmarkConfig, BenchmarkResult, SynthesisResult}
import utils.BenchmarkUtils.{getTimestamp, toBenchmarkResults}

import java.io.{File, IOException, PrintWriter}
import java.nio.file.{Files, Path}
import enumeration.Store
import enumeration.Enumerator

class BenchmarkRunner(val config: BenchmarkConfig) {
  implicit val formats: Formats = DefaultFormats.withEmptyValueStrategy(EmptyValueStrategy.preserve)
  val benchmarkPath: Path = config.getBenchmarkPath
  val timeout: Int = config.timeout
  val maxNameLen: Int = getMaxNameLen(this.benchmarkPath)
  var headerPrinted: Boolean = false
  val bothSolutions: Boolean = config.bothSolutions

  val resultsDir: String = config.outputDir.toString
  val csvWriter: PrintWriter = config.getCSVWriter

  def getMaxNameLen(file: Path): Int = {
    if (Files.isDirectory(file)) {
      file.toFile.listFiles().map(_.toPath).map(getMaxNameLen).max
    } else if (file.toString.endsWith(".sy")) {
      file.toString.length
    } else {
      0
    }
  }

  def printJson(result: BenchmarkResult): Unit = {
    val mut = result.mut.toString

    var fileName = s"${result.order}_${result.seen}_${mut}_${result.name.replace(File.separatorChar, '_').replace(".sy", ".json")}"
    var file = Path.of(this.resultsDir, fileName)

    try {
      var idx = 0
      while (Files.exists(file)) {
        // Append indices until it no longer exists.
        fileName = fileName.replace(".json", s"_$idx.json")
        file = Path.of(this.resultsDir, fileName)
        idx += 1
      }

      Files.createFile(file)
    } catch {
      case e: IOException =>
        System.err.println(s"Failed to create JSON file ${file.toAbsolutePath.toString}: $e")
        return
    }

    val writer = new PrintWriter(file.toFile)
    try {
      Serialization.writePretty(result, writer)
    } catch {
      case e: IOException => System.err.println(s"Failed to write to JSON file ${file.toAbsolutePath.toString}: $e")
    } finally {
      writer.close()
    }
  }

  def printResults(benchmark: Path, task: SynthesisTask, results: List[SynthesisResult]): Unit = {
    if (!this.headerPrinted) {
      this.headerPrinted = true

      // Terminal header
      print("  Type  | Subsumption | Name")
      print(" ".repeat(this.maxNameLen - 5))
      println("| Time (s) |       | Solution")
      print("--------+-------------+")
      print("-".repeat(this.maxNameLen))
      println("+----------+---------+---------------+------------------------------------------")

      // CSV file header
      try {
        this.csvWriter.println(
          "subsumption,name,time (ms),correct,store type,store size,subsumed by discarding,subsumed by replacing,solution,ismut"
        )
      } catch {
        case e: Throwable => System.err.println(e.toString)
      }
    }

    val name = {
      // This is a bit weird. We want the names _after_ the "benchmarks" directory.
      // But we can't assume the "benchmarks" directory is already part of the path
      val absolute = benchmark.toAbsolutePath.normalize()
      var benchmarksIndex = -1

      for (i <- Range(0, absolute.getNameCount)) {
        if (absolute.getName(i).toString == "benchmarks") {
          benchmarksIndex = i
        }
      }

      if (benchmarksIndex == -1) {
        benchmark.toString
      } else {
        absolute.subpath(benchmarksIndex + 1, absolute.getNameCount).toString
      }
    }

    results.map(toBenchmarkResults(name, task, _)).foreach {
      case rs @ BenchmarkResult(
            name,
            mut,
            solutionOpt,
            time,
            correct,
            storeSize,
            subsumedByDiscarding,
            subsumedByReplacing,
            storeType,
            _,
            seen,
            order
          ) =>
        val seenStr = seen.toString
        val timeStr = (time / 1000.0).formatted("%.3f")
        // val programsSeenStr = programsSeen.toString

        val solution = solutionOpt.getOrElse("Timeout")

        val (correctChar, correctStr) = {
          if (correct) (" ", "")
          else if (solutionOpt.isEmpty) (" ", "timeout")
          else if (task.solutions.isEmpty) (" ", "")
          else (" ", "")
        }

        val mutStr = solutionOpt match {
          case Some(_) => if (mut) "t" else "f"
          case None    => " "
        }

        val subsumption = (
          Store.subsumeByDiscarding,
          Store.subsumeByReplacement
        ) match {
          case (true, true)   => " both"
          case (true, false)  => " discarding"
          case (false, true)  => " replacing"
          case (false, false) => " none"
        }

        // Pretty-print to terminal
        print(" ")
        print(storeType)
        print(" ".repeat(8 - storeType.length))
        print(subsumption)
        print(" ".repeat(13 - subsumption.length + 2))
        print(name)
        print(" ".repeat(this.maxNameLen - name.length + 2))
        print(timeStr)
        print(" ".repeat(math.max(10 - timeStr.length, 1)))
        print(s"   $correctChar      ")
        println(s" [$mutStr] $solution")

        // CSV print to file
        // order,name,time,correct,programs seen,result
        this.csvWriter
          .println(
            s"$subsumption,\"$name\",$time,$correctStr,$storeType,$storeSize,$subsumedByDiscarding,$subsumedByReplacing,\"$solution\",$mutStr"
          )
        this.csvWriter.flush()

        // Finally, print everything to JSON!
        printJson(rs)
    }
  }

  def runBenchmark(task: SynthesisTask, path: Path, timeout: Int, print: Boolean = true): List[SynthesisResult] =
    doSynthesize(task, timeout, debug = false, bothSolutions = this.bothSolutions)

  def runBenchmarks(benchmarks: Path, timeout: Int, print: Boolean = true): Unit = try {
    if (Files.isDirectory(benchmarks)) {
      Files.list(benchmarks).sorted().forEachOrdered(runBenchmarks(_, timeout, print))
    } else if (Files.isReadable(benchmarks) && benchmarks.getFileName.toString.endsWith(".sy")) {
      val path = benchmarks
      val task = SynthesisTask.fromFile(path.toAbsolutePath.toString)
      val result = this.runBenchmark(task, path, timeout, print)
      if (print) printResults(path, task, result)
      System.gc()
    }
  } catch {
    case e: Throwable =>
      System.err.println(s"Failed to run benchmark: $benchmarks$e")
      e.printStackTrace(System.err)
  }

}

object BenchmarkRunner extends App {
  val defaultConfigStr =
    """{
      |    "benchmark": "src/test/benchmarks",
      |    "outputDirName": "defaultBenchmarkOutput",
      |    "timeout": 300,
      |    "bothSolutions": false,
      |    "testSubsumption": false
      |}""".stripMargin

  val config: BenchmarkConfig = if (args.isEmpty) {
    BenchmarkConfig.get(defaultConfigStr)
  } else {
    BenchmarkConfig.get(args.head)
  }
  val runner: BenchmarkRunner = new BenchmarkRunner(this.config)

  // First, warm up!
  println("Warming up. Please wait...")
  this.runner.runBenchmarks(this.runner.benchmarkPath, timeout = 5, print = false)
  System.gc()
  println("Warmup done. Starting benchmarks...")

  if (config.testSubsumption) {
    // We need to run all 4 variations
    for ((disc, repl) <- List((false, false), (true, true))) {
      Store.subsumeByDiscarding = disc
      Store.subsumeByReplacement = repl
      this.runner.runBenchmarks(this.runner.benchmarkPath, this.runner.timeout, print = true)
    }
  } else if (config.testOE) {
    for (oe <- List(Store.PureOE(), Store.NoOE())) {
      Store.storeType = oe
      this.runner.runBenchmarks(this.runner.benchmarkPath, this.runner.timeout, print = true)
    }
  } else {
    // Just run the basic test :)
    this.runner.runBenchmarks(this.runner.benchmarkPath, this.runner.timeout, print = true)
  }

  this.runner.csvWriter.close()
}
