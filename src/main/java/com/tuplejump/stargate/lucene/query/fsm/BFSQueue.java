package com.tuplejump.stargate.lucene.query.fsm;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * A priority queue for breadth-first search that can backtrack the list of states that lead to
 * some particular state.
 * <p>
 * Items are queued by their offsets, i.e., higher offsets are queued later.
 * 
 * @author Florian Leitner
 */
final class BFSQueue<E> {
  private QueueItem<Node<E>> start;
  private Map<QueueItem<Node<E>>, QueueItem<Node<E>>> moves;
  private Map<QueueItem<Node<E>>, Double> weights;
  private Queue<QueueItem<Node<E>>> queue;

  BFSQueue() {
    start = null;
    moves = new HashMap<QueueItem<Node<E>>, QueueItem<Node<E>>>();
    weights = new HashMap<QueueItem<Node<E>>, Double>();
    queue = new PriorityQueue<QueueItem<Node<E>>>();
  }

  BFSQueue(int offset, Node<E> init) {
    this();
    setStart(offset, init);
  }

  /**
   * Set the initial start state of search.
   * 
   * @param offset of this state in the input sequence
   * @param init the starting state to backtrack too
   */
  void setStart(int offset, Node<E> init) {
    if (init == null) throw new IllegalArgumentException("init state may never be null");
    if (queue.size() != 0 || moves.size() != 0)
      throw new IllegalStateException("tracer already running");
    start = new QueueItem<Node<E>>(offset, init);
    queue.add(start);
  }

  /**
   * Add the all target states that can be reached to the queue.
   * 
   * @param off of the target states in the input sequence
   * @param src queue item from where the transitions were made from
   * @param trgts states to where the source state transitioned to
   * @param w weighted IC gained from the {@link Transition}; should be zero for epsilon
   *        transitions
   */
  void addTransistions(int off, QueueItem<Node<E>> src, Set<Node<E>> trgts, double w) {
    w += src.equals(start) ? 0.0 : weights.get(src);
    for (Node<E> t : trgts) {
      if (t != null) {
        QueueItem<Node<E>> target = new QueueItem<Node<E>>(off, t);
        if (!moves.containsKey(target)) {
          moves.put(target, src);
          weights.put(target, w);
          queue.add(target);
        } else if (weights.get(target) < w) {
          // found a better move to the target with a higher IC weight
          moves.put(target, src);
          weights.put(target, w);
        }
      }
    }
  }

  /**
   * Find the list of states that were visited at each offset to reach this particular state.
   * 
   * @param from queue item from which to start backtracking to the start state
   * @return the list of queue items, starting with start, and ending with the <code>from</code>
   *         state
   */
  List<QueueItem<Node<E>>> backtrack(QueueItem<Node<E>> from) {
    List<QueueItem<Node<E>>> bt = new LinkedList<QueueItem<Node<E>>>();
    while (!from.equals(start)) {
      bt.add(from);
      if (!moves.containsKey(from))
        throw new NullPointerException("illegal item " + from.toString());
      from = moves.get(from);
    }
    bt.add(from);
    Collections.reverse(bt);
    return bt;
  }

  /** Return <code>true</code> if the queue is empty. */
  boolean isEmpty() {
    return queue.isEmpty();
  }

  /** Return the head of the queue. */
  QueueItem<Node<E>> remove() {
    return queue.remove();
  }
}
