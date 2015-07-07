package com.tuplejump.stargate.lucene.query.fsm;

/**
 * (Priority level, item) wrapper for the priority queue used in the breadth-first-search (BFS)
 * implementation of the {@link Matcher}.
 * <p>
 * It should never be necessary to directly use this class of the generic FSA implementation.
 * 
 * @author Florian Leitner
 */
final class QueueItem<T> implements Comparable<QueueItem<T>> {
  final int idx;
  final T item;

  /**
   * Create a new queue item that can be ordered by <code>index</code> (increasing).
   * 
   * @param index of the queue item (e.g., index position in the scanned sequence)
   * @param item the queue item itself
   */
  QueueItem(int index, T item) {
    this.idx = index;
    this.item = item;
  }

  /** Implements the Comparable interface by sorting on the <code>index</code> value. */
  public int compareTo(QueueItem<T> o) {
    return idx - o.idx;
  }

  /** Only compare the index and item. */
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    else if (!(o instanceof QueueItem)) return false;
    final QueueItem<?> other = (QueueItem<?>) o;
    return (idx == other.idx && item.equals(other.item));
  }

  /** Only use the index and item. */
  @Override
  public int hashCode() {
    int code = 17;
    code = 31 * code + idx;
    code = 31 * code + (item == null ? 0 : item.hashCode());
    return code;
  }

  /** Get the index of this item. */
  int index() {
    return idx;
  }

  /** Get the queue item itself. */
  T get() {
    return item;
  }

  @Override
  public String toString() {
    return String.format("%s[%d]=%s", QueueItem.class.getName(), idx, item.toString());
  }
}
