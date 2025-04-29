package utils

import enumeration.Enumerator

object AssertionInfo {
  def from(enum: Enumerator): List[AssertionInfo] = {
    (enum.store.currLevelMap.iterator ++ enum.store.prevLevelMap.iterator)
      .map { case (cond, idxs) =>
        AssertionInfo(cond, idxs.length)
      }
      .toList
      .sortBy(info => -info.size)
  }
}

case class AssertionInfo(cond: Condition, size: Int)
