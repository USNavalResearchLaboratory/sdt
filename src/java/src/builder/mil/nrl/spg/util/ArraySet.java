package builder.mil.nrl.spg.util;

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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * An ArrayList implementation of Set. An ArraySet is good for small sets; it has less overhead than a HashSet or a
 * TreeSet.
 * 
 * @author bowen
 * @since Jun 5, 2009
 * 
 * @param <E>
 */
public class ArraySet<E> extends AbstractSet<E>
{
	/**
	 * the backed array list
	 */
	protected final ArrayList<E> mItems;


	/**
	 * Create an empty set (default initial capacity is 3).
	 */
	public ArraySet()
	{
		this(3);
	}


	/**
	 * Create a set containing the items of the collection. Any duplicate items are discarded.
	 * 
	 * @param collection the source for the items of the small set
	 */
	public ArraySet(Collection<? extends E> collection)
	{
		mItems = new ArrayList<>(collection.size());
		for (E item : collection)
		{
			if (!mItems.contains(item))
			{
				mItems.add(item);
			}
		}
	}


	/**
	 * Create an empty set with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity
	 */
	public ArraySet(int initialCapacity)
	{
		mItems = new ArrayList<>(initialCapacity);
	}


	@Override
	public boolean add(E item)
	{
		if (mItems.contains(item))
		{
			return false;
		}
		else
		{
			return mItems.add(item);
		}
	}


	/**
	 * Get the item at the specified index.
	 * 
	 * @param index where the item is located in the ListSet
	 * @return the item at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public E get(int index) throws IndexOutOfBoundsException
	{
		return mItems.get(index);
	}


	@Override
	public Iterator<E> iterator()
	{
		return mItems.iterator();
	}


	@Override
	public int size()
	{
		return mItems.size();
	}
}
