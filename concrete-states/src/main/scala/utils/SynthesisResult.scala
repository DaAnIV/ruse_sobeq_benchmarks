package utils

import enumeration.Store

case class SynthesisResult(
    mut: Boolean,
    solution: Option[String],
    time: Long,
    storeSize: Int,
    programsSubsumedByDiscarding: Int,
    programsSubsumedByReplacement: Int,
    storeType: Store.StoreType,
    assertions: List[AssertionInfo]
)
