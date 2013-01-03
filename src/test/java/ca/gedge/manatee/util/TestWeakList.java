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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests methods of {@link WeakList}
 */
public class TestWeakList {
	@Test
	public void testAdd() {
		final Object a = new Object();
		final Object b = new Object();
		final Object c = new Object();

		final WeakList<Object> list = new WeakList<Object>();
		list.add(a);
		list.add(b);

		assertEquals(2, list.size());
		assertEquals(a, list.get(0));
		assertEquals(b, list.get(1));

		list.add(1, c);
		assertEquals(3, list.size());
		assertEquals(a, list.get(0));
		assertEquals(c, list.get(1));
		assertEquals(b, list.get(2));
	}

	@Test(expected=NullPointerException.class)
	public void testAddNull() {
		final WeakList<Object> list = new WeakList<Object>();
		list.add(null);
	}

	@Test
	public void testAddWeakRef() {
		Object a = new Object();
		final WeakList<Object> list = new WeakList<Object>();
		list.add(a);

		assertEquals(1, list.size());
		assertEquals(a, list.get(0));

		// Now force garbage collection of a to ensure its weak ref in the
		// list is invalidated
		a = null;
		System.gc();
		assertEquals(0, list.size());
	}

	@Test
	public void testRemove() {
		final Object a = new Object();
		final Object b = new Object();
		final Object c = new Object();

		final WeakList<Object> list = new WeakList<Object>();
		list.add(a);
		list.add(b);
		list.add(c);

		assertEquals(3, list.size());
		assertEquals(a, list.get(0));
		assertEquals(b, list.get(1));
		assertEquals(c, list.get(2));

		assertEquals(a, list.remove(0));
		assertEquals(2, list.size());
		assertEquals(b, list.get(0));
		assertEquals(c, list.get(1));

		assertEquals(true, list.remove(b));
		assertEquals(1, list.size());
		assertEquals(c, list.get(0));

		assertEquals(false, list.remove(b));
	}
}
