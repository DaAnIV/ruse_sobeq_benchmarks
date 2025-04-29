package utils

import enumeration.Store

case class SynthesisResult(
    mut: Boolean,
    solution: Option[String],
    rawSize: Option[Int],
    postProcessedSize: Option[Int],
    time: Long,
    storeSize: Int,
    programsSubsumedByDiscarding: Int,
    programsPrunedByOE: Int,
    storeType: Store.StoreType,
    assertions: List[AssertionInfo]
)
