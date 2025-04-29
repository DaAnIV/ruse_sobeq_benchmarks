import enumeration.Enumerator
import task.SynthesisTask
import utils.{PostProcessor, AssertionInfo, SynthesisResult}
import vocab.Vocabulary

import scala.concurrent.duration.{Deadline, DurationInt}
import enumeration.Store

object Main extends App {
  def doSynthesize(task: SynthesisTask, timeout: Int = 30, debug: Boolean = true, bothSolutions: Boolean = false): List[SynthesisResult] = {
    val vocab = Vocabulary.vocabFromTask(task)
    doSynthesize(task, timeout, debug, bothSolutions, vocab)
  }

  def doSynthesize(
      task: SynthesisTask,
      timeout: Int,
      debug: Boolean,
      bothSolutions: Boolean,
      vocabulary: Vocabulary
  ): List[SynthesisResult] = {
    val deadline: Deadline = timeout.seconds.fromNow
    val enumerator = new Enumerator(vocabulary, task, deadline)

    var mutResult: Option[SynthesisResult] = None
    var immutResult: Option[SynthesisResult] = None

    for ((program, i) <- enumerator.zipWithIndex) {
      val store = enumerator.store
      if (debug && i % 10000 == 0) {
        val code = PostProcessor.process(program, store).code
        println(s"$i: $code {${program.values.mkString("[", ",", "]")}}")
      }

      if (task(program)) {
        // We found _a_ solution
        val postprocessed = PostProcessor.process(program, store)
        val code = postprocessed.code
        val time = timeout * 1000 - deadline.timeLeft.toMillis

        if (debug) {
          println(s"$i: $code")
        }

        if (!bothSolutions) {
          // We are done!
          return List(
            SynthesisResult(
              program.isMut,
              Some(code),
              Some(program.astSize),
              Some(postprocessed.astSize),
              time,
              store.size,
              store.subsumedByDiscarding,
              store.prunedByOEPure,
              store.prunedByOEMut,
              Store.storeType,
              AssertionInfo.from(enumerator)
            )
          )
        }

        val isMut = program.isMut

        if (isMut && mutResult.isEmpty) {
          mutResult = Some(
            SynthesisResult(
              isMut,
              Some(code),
              Some(program.astSize),
              Some(postprocessed.astSize),
              time,
              store.size,
              store.subsumedByDiscarding,
              store.prunedByOEPure,
              store.prunedByOEMut,
              Store.storeType,
              AssertionInfo.from(enumerator)
            )
          )
        } else if (!isMut && immutResult.isEmpty) {
          immutResult = Some(
            SynthesisResult(
              isMut,
              Some(code),
              Some(program.astSize),
              Some(postprocessed.astSize),
              time,
              store.size,
              store.subsumedByDiscarding,
              store.prunedByOEPure,
              store.prunedByOEMut,
              Store.storeType,
              AssertionInfo.from(enumerator)
            )
          )
        }

        if (mutResult.isDefined && immutResult.isDefined) {
          return List(mutResult.get, immutResult.get)
        }
      }
    }

    val timeoutRes = SynthesisResult(
      mut = mutResult.isEmpty,
      None,
      None,
      None,
      timeout * 1000 - deadline.timeLeft.toMillis,
      enumerator.store.size,
      enumerator.store.subsumedByDiscarding,
      enumerator.store.prunedByOEPure,
      enumerator.store.prunedByOEMut,
      Store.storeType,
      AssertionInfo.from(enumerator)
    )

    (mutResult, immutResult) match {
      case (Some(mut), Some(immut)) => List(mut, immut)
      case (Some(mut), None)        => List(mut, timeoutRes)
      case (None, Some(immut))      => List(timeoutRes, immut)
      case _                        => List(timeoutRes)
    }
  }

  val task = SynthesisTask.fromFile(args(0))
  val timeout = if (args.length > 1) {
    args(1).toInt
  } else {
    30
  }

  doSynthesize(task, timeout)
}
