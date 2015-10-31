package com.tuplejump.stargate.lucene.query.fsm;

/**
 * Transitions define how elements of a sequence should match to the pattern.
 *
 * E.g., if the {@link Pattern} (NFA) should match a List of elements of some generic type
 * <code>E</code> (i.e., a <code>List&lt;E&gt;</code> ), transition instances determine if the
 * current element in the sequence being aligned is appropriate to allow the automaton to move on
 * (transition) to its next state.
 *
 * In the case of a character sequence automaton (i.e., in String pattern matching such as provided
 * by Java's regex package), the {@link Transition#matches(Object)} implementation would return the
 * Boolean result of {@link Character#compareTo(Character)} <code>== 0</code> and be instantiated
 * by defining the relevant character for the transition. Furthermore, for the backtracking
 * mechanism provided by the {@link Pattern} NFA, a {@link #weight() weight} for each transition
 * must be defined, while epsilon transitions should default to a weight of zero. The path chosen
 * for backtracking then is the path that has the highest summed transition weights and therefore
 * identifies the matched sequence and capture groups.
 *
 * <pre>
 * class CharacterTransition implements Transition&lt;Character&gt; {
 *   private Character c;
 *
 *   public CharacterTransition(Character toMatch) {
 *     this.c = toMatch;
 *   }
 *
 *   public boolean matches(Character other) {
 *     return c.compareTo(other) == 0;
 *   }
 *
 *   public double weight() {
 *     return 1.0; // each character is of equal &quot;weight&quot;
 *   }
 * }
 * </pre>
 *
 * @author Florian Leitner
 */
public interface Transition<E> {
    /**
     * A transition implementation must define if, given some unknown element, it is valid to make
     * the transition or not.
     *
     * @param element an element from the sequence being matched
     * @return <code>true</code> if the transition is valid, <code>false</code> otherwise.
     */
    public boolean matches(E element);

    /**
     * A transition implementation can define a weight that is used in backtracking to evalute the
     * path with the highest IC.
     *
     * @return a value that represents the information content of this transition
     */
    public double weight();

    /**
     * Any side effect operation that needs to be done when a match happens,like triggering or
     * setting something on the element itself
     *
     * @param element
     */
    public void onMatch(E element);
}
