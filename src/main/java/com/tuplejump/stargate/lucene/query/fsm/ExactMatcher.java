/* Created on Feb 1, 2013 by Florian Leitner.
 * Copyright 2013. All rights reserved. */
package com.tuplejump.stargate.lucene.query.fsm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This matcher implements sequence comparison between <i>Lists</i> using the <b>Boyer-Moore</b>
 * pattern matching algorithm.
 * <p>
 * As this is an exact matcher, elements are compared using <code>equals(Object)</code>. Note that
 * the empty pattern is illegal, while a <code>null</code> in the pattern is allowed to match a
 * <code>null</code> in the sequence if at the right position.
 * 
 * @author Florian Leitner
 */
public final class ExactMatcher<E> extends MatcherBase<E> {
  /** The Boyer-Moore mismatch jump table. */
  private final Map<E, Integer> shifts;
  /** The Boyer-Moore suffix match jump table. */
  private final int[] suffix;

  /**
   * Create a matcher for a pattern sequence, preprocessing the offset and suffix jump tables.
   * 
   * @param pattern sequence that should lead to a match
   * @throws IllegalArgumentException if the pattern is empty
   */
  public ExactMatcher(final List<E> pattern) {
    super(pattern);
    shifts = new HashMap<E, Integer>();
    suffix = new int[end];
    int prefixPos = end;
    int i;
    // populate and compute the mismatch jump table:
    for (i = 0; i < end - 1; i++)
      shifts.put(this.pattern.get(i), end - 1 - i);
    // populate the suffix match jump table:
    for (i = end - 1; i >= 0; i--) {
      if (isPrefix(i + 1)) prefixPos = i + 1;
      suffix[end - 1 - i] = prefixPos - i + end - 1;
    }
    // compute the suffix match jumps:
    for (i = 0; i < end - 1; i++) {
      int slen = suffixLength(i);
      suffix[slen] = end - 1 - i + slen;
    }
  }

  /** Check if the elements after <code>index</code> are also a prefix of the pattern. */
  private boolean isPrefix(int index) {
    for (int pointer = 0; index < end; ++index, ++pointer)
      if (!match(pattern.get(index), pointer)) return false;
    return true;
  }

  /** Check if the item at <code>index</code> in the pattern equals <code>element</code>. */
  private boolean match(final E element, final int index) {
    return (element != null && element.equals(pattern.get(index)) || element == null &&
        pattern.get(index) == null);
  }

  /**
   * Returns the length of a sub-pattern that ends at <code>index</code> and also is the pattern's
   * suffix.
   */
  private int suffixLength(int index) {
    int slen = 0;
    for (int pointer = end - 1; index >= 0; --index, --pointer)
      if (match(pattern.get(index), pointer)) slen++;
      else break;
    return slen;
  }

  /**
   * Find the index at which the pattern matches in the <code>sequence</code>.
   * 
   * @param sequence list to align the pattern with
   * @return the offset of the match or <code>-1</code> if no match is found
   * @see ExactMatcher#find(List, int)
   */
  public int find(final List<E> sequence) {
    return find(sequence, 0);
  }

  /**
   * Find the index at which the pattern matches in the <code>sequence</code> at or after the
   * <code>offset</code>.
   * <p>
   * Find uses the <b>Booyer-Moore</b> algorithm; Therefore, the approach will use index access (
   * <code>get(int)</code> ) on the List, so it is recommended not to use linked lists as input.
   * 
   * @param sequence list to align the pattern with
   * @param offset index in sequence where to start the alignments
   * @return the offset of the match or <code>-1</code> if no match is found
   */
  public int find(final List<E> sequence, int offset) {
    final int size = sequence.size();
    int pointer;
    for (offset += end - 1; offset < size;) {
      for (pointer = end - 1; match(sequence.get(offset), pointer); --offset, --pointer)
        if (pointer == 0) return offset;
      E e = sequence.get(offset);
      offset += Math.max(suffix[end - 1 - pointer], (shifts.containsKey(e)) ? shifts.get(e) : end);
    }
    return -1;
  }

  @Override
  public int radix() {
    return shifts.size();
  }
}
