/* Created on Dec 20, 2012 by Florian Leitner. Copyright 2012. All rights reserved. */
package com.tuplejump.stargate.lucene.query.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for <b>exact pattern matching</b> implementations.
 * 
 * @author Florian Leitner
 */
abstract class MatcherBase<E> {
  /** The pattern sequence being matched. */
  protected final List<E> pattern;
  /** The length of the pattern. */
  final int end;

  /**
   * Create a new exact matcher for a pattern sequence.
   * 
   * @param pattern sequence that should lead to a match
   * @throws IllegalArgumentException if the pattern is empty
   */
  public MatcherBase(final List<E> pattern) {
    end = pattern.size();
    this.pattern = new ArrayList<E>(pattern);
    if (end == 0) throw new IllegalArgumentException("empty patterns are illegal");
  }

  /** Returns a copy of the pattern. */
  public List<E> pattern() {
    return new ArrayList<E>(pattern);
  }

  /** Returns the length of the pattern (total number of elements). */
  public int length() {
    return end;
  }

  /** Return the radix of the pattern (number of non-equal elements). */
  public abstract int radix();
}
