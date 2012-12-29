/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
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
