/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ca.gedge.manatee.util;

/**
 * A 2-tuple.
 * 
 * @param <F>  type of the first element
 * @param <S>  type of the second element
 */
public class Pair<F, S> implements Comparable<Pair<? extends F, ? extends S>> {
	/** The first element */
	private F first;

	/** The second element */
	private S second;

	/**
	 * Constructs a pair with <code>null</code> elements.
	 */
	public Pair() {
		this(null, null);
	}

	/**
	 * Constructs a pair with a specified first/second object.
	 * 
	 * @param first  the first element
	 * @param second  the second element
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Gets the first element.
	 * 
	 * @return the first element in this pair
	 */
	public F getFirst() {
		return first;
	}

	/**
	 * Sets the first element.
	 * 
	 * @param first  the first element
	 */
	public void setFirst(F first) {
		this.first = first;
	}

	/**
	 * Gets the second element.
	 * 
	 * @return the second element in this pair
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * Sets the second element.
	 * 
	 * @param second  the second element
	 */
	public void setSecond(S second) {
		this.second = second;
	}

	/**
	 * Gets whether or not two objects are both <code>null</code>, or if they
	 * are equal, as defined by their implementation of {@link Object#equals(Object)}.
	 * 
	 * @param a  an object
	 * @param b  an object
	 * 
	 * @return <code>true</code> if objects both <code>null</code> or equal,
	 *         <code>false</code> otherwise
	 */
	public static boolean objectsEqual(Object a, Object b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	//
	// Overrides
	//

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null) return false;

		if(o instanceof Pair) {
			final Pair<?, ?> b = (Pair<?, ?>)o;
			return objectsEqual(first, b.first) && objectsEqual(second, b.second);
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int hash1 = (first == null ? 0 : first.hashCode());
		final int hash2 = (second == null ? 0 : second.hashCode());
		return 19*hash1 + 31*hash2;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		builder.append('(');
		builder.append(String.valueOf(first));
		builder.append(", ");
		builder.append(String.valueOf(second));
		builder.append(')');

		return builder.toString();
	}

	//
	// Comparable
	//

	@SuppressWarnings("unchecked")
	private <T> int compare(T a, T b) {
		if(a == null && b == null) return 0;
		if(a == null) return 1;
		if(b == null) return -1;

		if(a instanceof Comparable)
			return ((Comparable<T>)a).compareTo(b);

		return (a.equals(b) ? 0 : 1); 
	}

	@Override
	public int compareTo(Pair<? extends F, ? extends S> o) {
		final int firstComp = compare(first, o.first);
		if(firstComp == 0)
			return compare(second, o.second);
		return firstComp;
	}	
}
