package utils

import enumeration.{ChildrenIterator, EnumerationOrderGenerator}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Formats}
import task.SynthesisTask
import utils.BenchmarkUtils.getTimestamp

import java.io.{FileNotFoundException, IOException, PrintWriter}
import java.nio.file.{Files, InvalidPathException, Path}
import java.time.{LocalDateTime, ZoneId}
import enumeration.Store

case class JSONConfig(
    benchmark: String,
    outputDirName: String,
    timeout: Int,
    bothSolutions: Option[Boolean],
    testSubsumption: Option[Boolean],
    testOE: Option[Boolean]
)

object BenchmarkConfig {
  def get(json: String): BenchmarkConfig = {
    implicit val formats: Formats = DefaultFormats
    val obj: JSONConfig = JsonMethods.parse(json).extract[JSONConfig]
    new BenchmarkConfig(obj)
  }
}

class BenchmarkConfig(conf: JSONConfig) {
  def getBenchmarkPath: Path = {
    try {
      val path = Path.of(conf.benchmark)
      if (Files.exists(path)) {
        path
      } else {
        System.err.println(f"Error: Benchmark path does not exist: ${path.toAbsolutePath}")
        System.exit(1)
        null
      }
    } catch {
      case _: InvalidPathException =>
        System.err.println(f"Error: Invalid benchmark path ${conf.benchmark}")
        System.exit(1)
        null
    }
  }

  lazy val timeout: Int = this.conf.timeout

  lazy val testSubsumption: Boolean = this.conf.testSubsumption.getOrElse(false)

  lazy val testOE: Boolean = this.conf.testOE.getOrElse(false)

  lazy val bothSolutions: Boolean = this.conf.bothSolutions.getOrElse(false)

  lazy val outputDir: Path = {
    val timestamp = getTimestamp()

    val resultsDirPath = Path.of(s"$timestamp-${this.conf.outputDirName}/")
    try {
      Files.createDirectories(resultsDirPath)
      resultsDirPath
    } catch {
      case _: IOException =>
        System.err.println(s"Failed to create results directory: ${resultsDirPath.toAbsolutePath.toString}")
        System.exit(1)
        null
    }
  }

  def getCSVWriter: PrintWriter = {
    val csvFilePath = Path.of(this.outputDir.toString, "results.csv")
    try {
      Files.createFile(csvFilePath)
      new PrintWriter(csvFilePath.toFile)
    } catch {
      case _: FileNotFoundException =>
        System.err.println(s"Failed to create output CSV file: ${csvFilePath.toAbsolutePath.toString}")
        System.exit(1)
        null
      case e: IOException =>
        System.err.println(s"Failed to create output CSV file: $e")
        System.exit(1)
        null
    }
  }
}

case class BenchmarkResult(
    name: String,
    mut: Boolean,
    solution: Option[String],
    time: Long,
    correct: Boolean,
    storeSize: Int,
    subsumedByDiscarding: Int,
    subsumedByReplacing: Int,
    storeType: String,
    regions: List[AssertionInfo],
    seen: Boolean,
    order: String
)

object BenchmarkUtils {
  def toBenchmarkResults(name: String, task: SynthesisTask, result: SynthesisResult): BenchmarkResult = result match {
    case SynthesisResult(
          mut,
          solution,
          time,
          storeSize,
          subsumedByDiscarding,
          subsumedByReplacing,
          storeType,
          regions
        ) =>
      BenchmarkResult(
        name,
        mut,
        solution,
        time,
        correct = solution.isDefined && task.solutions.contains(solution.get),
        storeSize,
        subsumedByDiscarding,
        subsumedByReplacing,
        storeType.toString(),
        regions,
        ChildrenIterator.useSeenSet,
        EnumerationOrderGenerator.currentOrder
      )
  }

  def getTimestamp(time: LocalDateTime = LocalDateTime.now()): String = {
    // TODO (kas) is this right? Getting a timestamp is tricky.
    val zone = ZoneId.systemDefault()
    val offset = zone.getRules.getOffset(time)
    time.toEpochSecond(offset).toString
  }
}
