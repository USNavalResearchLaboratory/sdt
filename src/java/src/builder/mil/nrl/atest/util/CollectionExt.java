/*
 *  =======================================================================
 *  ==                                                                   ==
 *  ==                   Classification: UNCLASSIFIED                    ==
 *  ==                   Classified By:                                  ==
 *  ==                   Declassify On:                                  ==
 *  ==                                                                   ==
 *  =======================================================================
 *
 *  Developed by:
 *
 *  Naval Research Laboratory
 *  Tactical Electronic Warfare Division
 *  Electronic Warfare Modeling and Simulation Branch
 *  Advanced Tactical Environmental Simulation Team (Code 5774)
 *  4555 Overlook Ave SW
 *  Washington, DC 20375
 *
 *  For more information call (202)767-2897 or send email to
 *  BuilderSupport@nrl.navy.mil.
 *
 *  The U.S. Government retains all rights to use, duplicate,
 *  distribute, disclose, or release this software.
 */

package builder.mil.nrl.atest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;

import builder.mil.nrl.spg.util.CollectionsExt;

/**
 * @author mamaril
 * @since Feb 18, 2009
 * 
 */
public class CollectionExt<E> implements Collection<E>
{
	protected final Collection<E> mCollection;


	public CollectionExt()
	{
		this(new ArrayList<E>());
	}


	public CollectionExt(Collection<E> backing)
	{
		mCollection = backing;
	}


	@Override
	public int size()
	{
		return this.mCollection.size();
	}


	@Override
	public boolean isEmpty()
	{
		return this.mCollection.isEmpty();
	}


	@Override
	public boolean contains(Object o)
	{
		return this.mCollection.contains(o);
	}


	/**
	 * Will search this collection for the supplied element instance. If the element is in this collection, true will be
	 * returned. If the element is not in this collection, false will be returned.
	 * <p>
	 * This method uses the equality operator (==) for comparison; it does not use the equals method like
	 * {@link Collection#contains(Object)} does. Other than this difference, these two methods perform the same task.
	 * <p>
	 * 
	 * @param obj the element to search for; can be null
	 * @return true if the element is in this collection; false if it is not.
	 */
	public boolean containsInstance(E obj)
	{
		for (E t : this)
		{
			if (t == obj)
			{
				return true;
			}
		}
		return false;
	}


	public boolean disjoint(Collection<?> c2)
	{
		return Collections.disjoint(mCollection, c2);
	}


	public Enumeration<E> enumeration()
	{
		final Iterator<E> it = iterator();
		return new Enumeration<E>()
			{
				@Override
				public boolean hasMoreElements()
				{
					return it.hasNext();
				}


				@Override
				public E nextElement()
				{
					return it.next();
				}
			};
	}


	public int frequency(E o)
	{
		return Collections.frequency(mCollection, o);
	}


	public E min(Comparator<? super E> comp)
	{
		return Collections.min(mCollection, comp);
	}


	public E max(Comparator<? super E> comp)
	{
		return Collections.max(mCollection, comp);
	}


	public <F extends E> F findFirst(Class<F> type)
	{
		return CollectionsExt.findFirst(mCollection, type);
	}


	public <F extends E> ListExt<F> find(Class<F> type)
	{
		return new ListExt<>(CollectionsExt.find(mCollection, type));
	}


	public <G> ListExt<G> findAny(Class<G> type)
	{
		return new ListExt<>(CollectionsExt.findAny(mCollection, type));
	}


	@Override
	public Iterator<E> iterator()
	{
		return this.mCollection.iterator();
	}


	@Override
	public Object[] toArray()
	{
		return this.mCollection.toArray();
	}


	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.mCollection.toArray(a);
	}


	@Override
	public String toString()
	{
		return this.mCollection.toString();
	}


	@Override
	public boolean add(E e)
	{
		return this.mCollection.add(e);
	}


	@Override
	public boolean remove(Object o)
	{
		return this.mCollection.remove(o);
	}


	/**
	 * Removes the first occurrence of the supplied element instance from this collection.
	 * <p>
	 * This method uses the equality operator (==) for comparison; it does not use the equals method like
	 * {@link Collection#remove(Object)} does. Other than this difference, these two methods perform the same task.
	 * <p>
	 * 
	 * @param obj the element to search for; can be null
	 * @return true if this collection was modified (the element was found in and removed from this collection); false
	 *         if this collection was not modified (the element was not found in, and thus, not removed from this
	 *         collection)
	 */
	public boolean removeInstance(E obj)
	{
		return removeInstanceInternal(obj, false);
	}


	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.mCollection.containsAll(c);
	}


	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return this.mCollection.addAll(c);
	}


	public boolean addAll(E... elements)
	{
		return Collections.addAll(mCollection, elements);
	}


	@Override
	public boolean removeAll(Collection<?> c)
	{
		return this.mCollection.removeAll(c);
	}


	/**
	 * Removes all occurrences of the supplied element instance from this collection.
	 * <p>
	 * This method uses the equality operator (==) for comparison; it does not use the equals method like
	 * {@link Collection#remove(Object)} does.
	 * <p>
	 * 
	 * @param obj the element to search for; can be null
	 * @return true if this collection was modified (the element was found in and removed from this collection); false
	 *         if this collection was not modified (the element was not found in, and thus, not removed from this
	 *         collection)
	 */
	public boolean removeAllInstances(E obj)
	{
		return removeInstanceInternal(obj, true);
	}


	/**
	 * Internal method to remove either the first instance of, or all instances of, the supplied element from this
	 * collection.
	 * 
	 * @param obj the element instance to remove
	 * @param removeAll true to remove all instances of the element from this collection; false to remove only the first
	 *        occurrence of the element from this collection
	 * @return true if this collection was modified (the element was found in and removed from this collection); false
	 *         if this collection was not modified (the element was not found in, and thus, not removed from this
	 *         collection)
	 */
	private boolean removeInstanceInternal(Object obj, boolean removeAll)
	{
		if (isEmpty())
		{
			return false;
		}

		boolean modified = false;

		Iterator<?> iter = iterator();
		while (iter.hasNext())
		{
			Object t = iter.next();

			if (obj == t)
			{
				iter.remove();
				if (removeAll == false)
				{
					return true;
				}

				modified = true;
			}
		}

		return modified;
	}


	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.mCollection.retainAll(c);
	}


	public boolean removeNulls()
	{
		Iterator<E> itr = mCollection.iterator();
		boolean removed = false;
		while (itr.hasNext())
		{
			E item = itr.next();
			if (item == null)
			{
				itr.remove();
				removed = true;
			}
		}
		return removed;
	}


	@Override
	public void clear()
	{
		this.mCollection.clear();
	}


	public CollectionExt<E> unmodifiable()
	{
		return new CollectionExt<>(Collections.unmodifiableCollection(mCollection));
	}


	public CollectionExt<E> synchronizedCollection()
	{
		return new CollectionExt<>(Collections.synchronizedCollection(mCollection));
	}


	public CollectionExt<E> checkedCollection(Class<E> type)
	{
		return new CollectionExt<>(Collections.checkedCollection(mCollection, type));
	}


	@Override
	public boolean equals(Object obj)
	{
		return mCollection.equals(obj);
	}


	@Override
	public int hashCode()
	{
		return mCollection.hashCode();
	}
}
