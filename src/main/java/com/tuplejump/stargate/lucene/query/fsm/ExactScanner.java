/* Created on Feb 1, 2013 by Florian Leitner.
 * Copyright 2013. All rights reserved. */
package com.tuplejump.stargate.lucene.query.fsm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This scanner implements sequence comparison over <i>Iterators</i> using the
 * <b>Knuth-Morris-Pratt</b> pattern matching algorithm.
 * <p>
 * As this is an exact matcher, elements are compared using <code>equals(Object)</code>. Note that
 * the empty pattern is illegal, while a <code>null</code> in the pattern is allowed to match a
 * <code>null</code> in the sequence if at the right position.
 * 
 * @author Florian Leitner
 */
public final class ExactScanner<E> extends MatcherBase<E> {
  /** The KMP 'alphabetized' transition tables. */
  private final Map<E, int[]> dfa;

  /**
   * Create a scanner for a pattern sequence, preprocessing the transition tables.
   * 
   * @param pattern sequence that should lead to a match
   * @throws IllegalArgumentException if the pattern is empty
   */
  public ExactScanner(final List<E> pattern) {
    super(pattern);
    dfa = new HashMap<E, int[]>();
    // initialize the transition tables:
    int[] next;
    for (E transition : pattern)
      dfa.put(transition, new int[end]);
    dfa.get(pattern.get(0))[0] = 1; // initial state match transition
    // calculate the transitions:
    for (int base = 0, pointer = 1; pointer < end; pointer++) {
      for (int[] change : dfa.values())
        change[pointer] = change[base]; // set state changes for mismatches
      next = dfa.get(this.pattern.get(pointer)); // get the table for the current element
      next[pointer] = pointer + 1; // store state change for match
      base = next[base]; // update current base state
    }
  }
  
  /** Convenience method to construct the scanner from an iterator. */
  public ExactScanner(final Iterator<E> pattern) {
    this(newLinkedList(pattern));
  }

  /** Return a linked list of the content in <code>iterator</code> . */
  private static <E> LinkedList<E> newLinkedList(Iterator<E> iterator) {
    LinkedList<E> ll = new LinkedList<E>();
    while (iterator.hasNext()) ll.add(iterator.next());
    return ll;
  }

  /** Returns an updated <code>pointer</code> using the transition table */
  private int transition(final E element, final int pointer) {
    // if the element is known, and given the current state (pointer), find the next (pointer)
    if (dfa.containsKey(element)) return dfa.get(element)[pointer];
    else return 0; // otherwise, return the initial state (pointer)
  }

  @Override
  public int radix() {
    return dfa.size();
  }

  /**
   * Determine if the pattern matches anywhere in a stream.
   * <p>
   * The iteration will halt <i>after</i> the last element of a valid pattern has been found or
   * consumes the entire stream otherwise.
   * 
   * @param seqIt the sequence stream to scan
   * @return <code>true</code> if the stream contained the pattern
   */
  public boolean scan(final Iterator<E> seqIt) {
    int pointer = 0;
    while (seqIt.hasNext()) {
      pointer = transition(seqIt.next(), pointer);
      if (pointer == end) return true;
    }
    return false;
  }
}
