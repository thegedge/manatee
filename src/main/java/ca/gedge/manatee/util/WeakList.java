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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list that holds weak references to objects. When retrieving an object
 * in any manner, be it through list methods or even the iterator, references
 * will be removed if their referring object is <code>null</code>. The
 * implication here is that <code>null</code> cannot be stored. Some things
 * to keep in mind when using this class:
 * <ul>
 *   <li>All methods that add objects to the list throw
 *       {@link UnsupportedOperationException} if <code>null</code> is one
 *       of the objects being added.</li>
 *   <li>Retrieval methods remove references that have become <code>null</code>,
 *       therefore retrieval methods can also modify the internal list.</li>
 *   <li>Iterators returned by this class will try to take care of skipping
 *       <code>null</code> references, but there is always a chance the GC
 *       will start up in the middle of things so always check for
 *       <code>null</code> to be safe.</li>
 *   <li>To ensure a certain level of correctness in return values,
 *       {@link WeakList#size()} also removes any <code>null</code>
 *       references. By "certain level" I mean that references could become
 *       <code>null</code> after being checked.</li>
 * </ul>
 * 
 * @param <T>  the type of objects being stored in the list 
 */
public class WeakList<T> implements List<T> {
	/** The list backing this implementation */
	private List<WeakReference<T>> internalList = new ArrayList<WeakReference<T>>();

	@Override
	public boolean add(T element) {
		add(size(), element);
		return true;
	}

	@Override
	public void add(int index, T element) {
		if(element == null)
			throw new NullPointerException("Null values cannot be inserted into a WeakList");

		synchronized(internalList) {
			internalList.add(index, new WeakReference<T>(element));
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return addAll(size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		// Create a list of weak references
		ArrayList<WeakReference<T>> weakCollection = new ArrayList<WeakReference<T>>();
		for(T element : c) {
			if(element == null)
				throw new NullPointerException("Null values cannot be inserted into a WeakList");

			weakCollection.add(new WeakReference<T>(element));
		}

		// Call internal list's addAll
		synchronized(internalList) {
			return internalList.addAll(index, weakCollection);
		}
	}

	@Override
	public void clear() {
		synchronized(internalList) {
			internalList.clear();
		}
	}

	@Override
	public boolean contains(Object o) {
		if(o == null) // null cannot be stored
			return false;

		synchronized(internalList) {
			for(WeakReference<T> obj : internalList)
				if(o.equals(obj.get()))
					return true;
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		synchronized(internalList) {
			for(WeakReference<T> obj : internalList) {
				if(!c.contains(obj))
					return false;
			}
		}
		return true;
	}

	@Override
	public T get(int index) {
		T ret = null;
		synchronized(internalList) {
			while(ret == null) {
				ret = internalList.get(index).get();
				if(ret == null)
					internalList.remove(index);
			}
		}
		return ret;
	}

	@Override
	public int indexOf(Object o) {
		if(o != null) {
			synchronized(internalList) {
				for(int i = 0; i < internalList.size(); ++i) {
					final T obj = internalList.get(i).get(); 
					if(o.equals(obj))
						return i;
				}
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return (size() == 0);
	}

	@Override
	public Iterator<T> iterator() {
		return new ReferenceIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		if(o != null) {
			synchronized(internalList) {
				for(int i = internalList.size() - 1; i >= 0; --i) {
					final T obj = internalList.get(i).get(); 
					if(o.equals(obj))
						return i;
				}
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ReferenceIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ReferenceIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		if(o != null) {
			synchronized(internalList) {
				for(int index = 0; index < internalList.size(); ++index) {
					final T obj = internalList.get(index).get(); 
					if(o.equals(obj)) {
						internalList.remove(index);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public T remove(int index) {
		T ret = null;
		synchronized(internalList) {
			do {
				ret = internalList.remove(index).get();
			} while(ret == null);
		}
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = false;
		synchronized(internalList) {
			for(Object o : c)
				if(remove(o))
					ret = true;
		}
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean ret = false;
		synchronized(internalList) {
			Iterator<WeakReference<T>> iter = internalList.iterator();
			while(iter.hasNext()) {
				final T obj = iter.next().get();
				if(obj == null || !c.contains(obj)) {
					iter.remove();
					ret = true;
				}
			}
		}
		return ret;
	}

	@Override
	public T set(int index, T element) {
		synchronized(internalList) {
			return internalList.set(index, new WeakReference<T>(element)).get();
		}
	}

	@Override
	public int size() {
		synchronized(internalList) {
			final Iterator<WeakReference<T>> iter = internalList.iterator();
			while(iter.hasNext()) {
				if(iter.next().get() == null)
					iter.remove();
			}
		}
		return internalList.size();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		// TODO implement this, as it's a view into this list
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[0]);
	}

	@Override
	public <E> E[] toArray(E[] a) {
		final ArrayList<T> objects = new ArrayList<T>();
		final Iterator<T> iter = iterator();
		while(iter.hasNext())
			objects.add(iter.next());
		return objects.toArray(a);
	}

	/**
	 * A simple extension to the standard iterator which skips weak references
	 * pointing to <code>null</code> objects. 
	 */
	private class ReferenceIterator implements ListIterator<T> {
		private ListIterator<WeakReference<T>> iter;

		public ReferenceIterator() {
			this(0);
		}

		public ReferenceIterator(int index) {
			this.iter = internalList.listIterator(index);
		}

		@Override
		public boolean hasNext() {
			while(iter.hasNext()) {
				if(iter.next().get() == null) {
					iter.remove();
				} else {
					iter.previous();
					break;
				}
			}
			return iter.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			while(iter.hasPrevious()) {
				if(iter.previous().get() == null) {
					iter.remove();
				} else {
					iter.next();
					break;
				}
			}
			return iter.hasPrevious();
		}

		@Override
		public T next() {
			T val = null;
			while(iter.hasNext() && val == null) {
				val = iter.next().get();
				if(val == null)
					iter.remove();
			}
			return val;
		}

		@Override
		public void remove() {
			iter.remove();
		}

		@Override
		public void add(T o) {
			if(o == null)
				throw new NullPointerException("Null values cannot be inserted into a WeakList");
			iter.add(new WeakReference<T>(o));
		}

		@Override
		public int nextIndex() {
			hasNext();
			return iter.nextIndex();
		}

		@Override
		public T previous() {
			T val = null;
			while(iter.hasPrevious() && val == null) {
				val = iter.previous().get();
				if(val == null)
					iter.remove();
			}
			return val;
		}

		@Override
		public int previousIndex() {
			hasPrevious();
			return iter.previousIndex();
		}

		@Override
		public void set(T o) {
			if(o == null)
				throw new NullPointerException("Null values cannot be inserted into a WeakList");
			iter.set(new WeakReference<T>(o));
		}
	}
}
