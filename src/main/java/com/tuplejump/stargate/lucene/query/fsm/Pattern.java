/* Created on Dec 21, 2012 by Florian Leitner.
 * Copyright 2012. All rights reserved. */
package com.tuplejump.stargate.lucene.query.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A <i>generic</i>, <b>NFA-based pattern matching</b> implementation using <i>weighted
 * backtracking</i> to provide capture groups.
 * <p>
 * This class provides methods to compile a non-deterministic state machine. In addition to a
 * pattern parser/compiler, the {@link Transition} interface has to be implemented, defining how
 * elements on the sequence should be matched and which weight the resulting transition should
 * have.
 * <p>
 * The entire API for this generic NFA is designed as close as possible to Java's own
 * {@link java.util.regex.Pattern} API. It is incomplete, because while the class is usable, it
 * provides no static <code>compile(String regex)</code> method or any other predetermined
 * mechanism of assembling the FSM. Therefore, some procedure of compiling the NFA needs to be
 * implemented, such as a regular expression language based on a context free grammar. For this
 * reason, the {@link #toString()} method that would convert the Pattern back to a String of the
 * regular language only produces a directed, acyclic graph of the state-transitions (i.e., cyclic
 * dependencies are not represented) that is useful to debug the state machine.
 * <p>
 * <b>Compiling a Pattern</b>
 * <p>
 * Implementations of this generic NFA package could, for example, inherit this class and implement
 * a method such as <code>static Pattern&lt;E&gt; compile(String)</code> that parses and compiles
 * the NFA from some regular language. The states should be compiled using the default constructors
 * or the static methods {@link Pattern#match(Transition) match} (a single transition), and then
 * joined with {@link Pattern#chain(Pattern, Pattern) chain} ("AND", "", i.e., chain a sequence of
 * transitions) or {@link Pattern#branch(Pattern, Pattern) branch} ("OR", "|", branch out into
 * several possible transitions) operations. To declare a particular sub-pattern as a capture
 * group, apply the static {@link Pattern#capture(Pattern) capture} method on it. Any (sub-)
 * pattern's behavior can be augmented by making it {@link #optional() optional} ( <code>?</code> )
 * and/or by allowing it to {@link Pattern#repeat() repeat} ( <code>+</code> ; a pattern that is
 * made both optional and repeated effectively acts as a full Kleene closure ( <code>*</code> )).
 * Unless there are reasons not to, the last step of compiling a pattern should be to call
 * {@link Pattern#minimize()} on itself, thereby removing states with epsilon transitions and no
 * other pattern semantics (essentially, removing artifacts created during the compilation).
 * <p>
 * A few convenience methods present in {@link java.util.regex.Pattern Java's Pattern API} are not
 * implemented, particularly the <code>split</code> methods.
 * 
 * @author Florian Leitner
 */
public class Pattern<E> {
  private Node<E> entry;
  private Node<E> exit;

  /**
   * Create a pattern that matches a single transition.
   * 
   * @param t the transition that has to match
   * @return a NFA
   */
  public static final <T> Pattern<T> match(Transition<T> t) {
    Node<T> entry = new Node<T>();
    Node<T> exit = new Node<T>();
    entry.addTransition(t, exit);
    return new Pattern<T>(entry, exit);
  }

  /**
   * Join two successive patterns into one ("AND", "").
   * 
   * @param first pattern to match before second
   * @param second pattern to match after first
   * @return a joined NFA
   */
  public static final <T> Pattern<T> chain(Pattern<T> first, Pattern<T> second) {
    first.exit.makeNonFinal();
    first.exit.addEpsilonTransition(second.entry);
    return new Pattern<T>(first.entry, second.exit);
  }

  /**
   * Fork out into one of two patterns ("OR", "|).
   * 
   * @param left optional pattern to match
   * @param right optional pattern to match
   * @return a forked NFA
   */
  public static final <T> Pattern<T> branch(Pattern<T> left, Pattern<T> right) {
    Node<T> entry = new Node<T>();
    Node<T> exit = new Node<T>();
    left.exit.makeNonFinal();
    right.exit.makeNonFinal();
    entry.addEpsilonTransition(left.entry);
    entry.addEpsilonTransition(right.entry);
    left.exit.addEpsilonTransition(exit);
    right.exit.addEpsilonTransition(exit);
    return new Pattern<T>(entry, exit);
  }

  /**
   * Make this pattern capturing, i.e., ensure the sequence offsets matched by it will be recored
   * as a capture group by the {@link Matcher}.
   * 
   * @param pattern to capture
   * @return a NFA
   */
  public static final <T> Pattern<T> capture(Pattern<T> pattern) {
    // note that a state with both the capture start and end flag set will be treated as
    // first ending a group, then starting a new one; therefore, if the pattern's entry and
    // exit states are the same (instance), additional states need to be introduced, otherwise the
    // the matcher would try to first end a (not yet started) group and then start a group
    // (that might never end) because the start and end flags would be set on the same state
    if (!pattern.entry.equals(pattern.exit) && !pattern.entry.captureStart &&
        !pattern.exit.captureEnd) {
      // entry and exit are not the same; simple case
      pattern.entry.captureStart = true;
      pattern.exit.captureEnd = true;
      return pattern;
    } else {
      Node<T> entry = new Node<T>();
      Node<T> exit = new Node<T>();
      entry.captureStart = true;
      exit.captureEnd = true;
      entry.addEpsilonTransition(pattern.entry);
      pattern.exit.addEpsilonTransition(exit);
      pattern.exit.makeNonFinal();
      return new Pattern<T>(entry, exit);
    }
  }

  /**
   * Construct the simplest possible NFA: a two-state automata joined by an epsilon transition.
   * <p>
   * This pattern will match anything, from the empty sequence ("lambda"), to the infinite one. It
   * provides a perfect "seed" for assembling more complex patterns.
   */
  public Pattern() {
    entry = new Node<E>();
    exit = new Node<E>();
    entry.addEpsilonTransition(exit);
    exit.makeFinal();
  }

  /**
   * Construct an NFA from the given entry and exit states.
   * <p>
   * It is the responsibility of the user to ensure these two states are actually connected. This
   * particular constructor should therefore only be used internally.
   * 
   * @param entry state
   * @param exit state
   */
  Pattern(Node<E> entry, Node<E> exit) {
    this.entry = entry;
    this.exit = exit;
    exit.makeFinal(); // ensure at least exit is a final state
  }

  /**
   * A tree-like (multi-line) DAG representation of the NFA's states and transitions for debugging
   * purposes only.
   */
  @Override
  public final String toString() {
    return String.format("Pattern:\n%s", entry.toString());
  }

  /**
   * Augment this pattern to match zero or one iterations of itself, i.e., the pattern may
   * optionally be skipped.
   * <p>
   * It is allowed to use both {@link #optional()} and {@link #repeat()} on the same pattern.
   * 
   * @return itself/this pattern
   */
  public final Pattern<E> optional() {
    entry.addEpsilonTransition(exit);
    return this;
  }

  /**
   * Augment this pattern to match one or more repetitions of itself, i.e., the pattern may
   * optionally be matched several times.
   * <p>
   * It is allowed to use both {@link #optional()} and {@link #repeat()} on the same pattern.
   * 
   * @return itself/this pattern
   */
  public final Pattern<E> repeat() {
    exit.addEpsilonTransition(entry);
    return this;
  }

  /**
   * Remove states that only have epsilon transitions and instead connect their source and target
   * states directly. Capture states will never be pruned even if they only have outgoing epsilon
   * transitions.
   * <p>
   * This method should be used after the entire pattern has been compiled to reduce the total
   * number of transitions and states in the FSM.
   * 
   * @return itself/this pattern
   */
  public final Pattern<E> minimize() {
    Node<E> node;
    Queue<Node<E>> queue = new LinkedList<Node<E>>(); // queue of states to check
    // a map of states with only epsilon transitions and their associated target states
    Map<Node<E>, Set<Node<E>>> invalidStates = new HashMap<Node<E>, Set<Node<E>>>();
    Set<Node<E>> validNodes = new HashSet<Node<E>>(); // states that should not be pruned
    // remove superfluous entry nodes (single epsilon transitions without any other semantics)
    while (entry.transitions.size() == 0 && entry.epsilonTransitions.size() == 1 &&
        !entry.isFinal() && !entry.isCapturing()) {
      node = entry.epsilonTransitions.iterator().next();
      if (node.equals(entry)) break;
      entry = node;
    }
    queue.add(entry);
    // detect invalid states: states that are non-final with no regular transitions
    // unless it is the entry state or a capture group-related state
    while (!queue.isEmpty()) {
      node = queue.remove();
      if (!node.isFinal() && !node.isCapturing() && node.transitions.size() == 0 &&
          !node.equals(entry)) {
        // for those invalid states, record their (epsilon transition) targets
        invalidStates.put(node, node.epsilonTransitions);
      } else {
        // everything else is a valid state
        validNodes.add(node);
      }
      // find yet unseen states to queue
      for (Node<E> next : node.epsilonTransitions) {
        if (!validNodes.contains(next) && !invalidStates.containsKey(next)) queue.add(next);
        if (next.equals(node)) // safeguard to avoid infinite loops
          throw new RuntimeException("circular reference detected: " + node.toString());
      }
      for (Set<Node<E>> nodeSet : node.transitions.values()) {
        for (Node<E> next : nodeSet) {
          if (!validNodes.contains(next) && !invalidStates.containsKey(next)) queue.add(next);
        }
      }
    }
    boolean pruning = true;
    // find invalid states that map to other invalid states and replace those
    // targets by continuously expanding them until they are only valid targets left
    while (pruning) {
      // while any invalid state contained a mapping to any other invalid state, keep pruning
      pruning = false; // assume pruning is done at the start of each round of pruning
      // iterate over all invalid states
      for (Node<E> source : invalidStates.keySet()) {
        // if replaceAndExpand is true, this source state was pointing to another invalid state
        if (replaceAndExpand(invalidStates, invalidStates.get(source))) pruning = true;
      }
    }
    // after pruning the pointers, we can now expand all invalid states pointed at by valid ones
    // with their appropriate valid target states
    for (Node<E> valid : validNodes) {
      replaceAndExpand(invalidStates, valid.epsilonTransitions);
      for (Set<Node<E>> targetNodes : valid.transitions.values())
        replaceAndExpand(invalidStates, targetNodes);
    }
    return this;
  }

  /**
   * Expand any invalid states in the given set of states.
   * 
   * @param expansions a mapping of invalid states to their target expansions
   * @param nodes a set of states possibly containing invalid states to be expanded
   * @return <code>true</code> if any expansion was made
   */
  private static final <T> boolean replaceAndExpand(Map<Node<T>, Set<Node<T>>> expansions,
      Set<Node<T>> nodes) {
    Node<T> s;
    Set<Node<T>> expansion = null; // be lazy - only instantiate this set if necessary
    Iterator<Node<T>> iter = nodes.iterator();
    // iterate over the states
    while (iter.hasNext()) {
      s = iter.next();
      if (expansions.containsKey(s)) {
        // the state is invalid: replace and expand with that state's expansions
        iter.remove();
        if (expansion == null) expansion = new HashSet<Node<T>>();
        expansion.addAll(expansions.get(s));
      }
    }
    if (expansion != null) {
      nodes.addAll(expansion);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Creates a matcher that will match the input sequence against this pattern.
   * 
   * @param input sequence to be matched
   * @return a new matcher for this pattern
   */
  public final Matcher<E> matcher(List<E> input) {
    return new Matcher<E>(entry, exit, input);
  }
  // XXX: possible future additions to make this class more equal to Java's Pattern API:
  // public final List<E>[] split(List<E> input)
  // public final List<E>[] split(List<E> input, int limit)
}
