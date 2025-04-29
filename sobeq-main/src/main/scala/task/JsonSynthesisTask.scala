package task
import org.json4s._

object Format {
  implicit val formats = DefaultFormats
}

case class JsonExample(
  input: Map[String,String],
  output: Option[String],
  state: Option[Map[String, String]]
)

case class JsonSynthesisTask(
    variables: Map[String,String],
    stringLiterals: List[String],
    intLiterals: List[Int],
    returnType: Option[String],
    immutable: Option[List[String]],
    examples: List[JsonExample],
    solution: List[String])