package utils

/**
  * Helper class for creating a reusable iterator.
  *
  * @param coll
  *   The collection we're iterating over
  */
case class CyclicIterator[T](val coll: Iterable[T]) extends Iterator[T] {
  var inner: Iterator[T] = coll.iterator

  override def hasNext: Boolean = this.inner.hasNext
  override def next(): T = this.inner.next()

  def reset(): Unit = {
    this.inner = coll.iterator
  }
}
