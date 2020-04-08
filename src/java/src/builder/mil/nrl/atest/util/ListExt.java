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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * @author mamaril
 * @since Feb 18, 2009
 * 
 */
public class ListExt<E> extends CollectionExt<E> implements List<E>
{
	/**
	 * See Collections#BINARYSEARCH_THRESHOLD.
	 */
	private static final int BINARYSEARCH_THRESHOLD = 5000;

	private static final ListExt empty = new ListExt(0);


	public static <T> ListExt<T> emptyList()
	{
		return empty;
	}


	public ListExt(final int initialCapacity)
	{
		this(new ArrayList<E>(initialCapacity));
	}


	public ListExt(final Collection<? extends E> items)
	{
		this(new ArrayList<E>(items));
	}


	public ListExt()
	{
		this(new ArrayList<E>());
	}


	public ListExt(List<E> backing)
	{
		super(backing);
	}


	private List<E> getBacking()
	{
		if (this.mCollection instanceof List)
		{
			return (List<E>) this.mCollection;
		}

		return new ArrayList<>(this.mCollection);
	}


	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		final List<E> list = getBacking();
		return list.addAll(index, c);
	}


	@Override
	public E get(int index)
	{
		final List<E> list = getBacking();
		return list.get(index);
	}


	@Override
	public E set(int index, E element)
	{
		final List<E> list = getBacking();
		return list.set(index, element);
	}


	@Override
	public void add(int index, E element)
	{
		final List<E> list = getBacking();
		list.add(index, element);
	}


	@Override
	public E remove(int index)
	{
		final List<E> list = getBacking();
		return list.remove(index);
	}


	@Override
	public int indexOf(Object o)
	{
		final List<E> list = getBacking();
		return list.indexOf(o);
	}


	@Override
	public int lastIndexOf(Object o)
	{
		final List<E> list = getBacking();
		return list.lastIndexOf(o);
	}


	@Override
	public ListIterator<E> listIterator()
	{
		final List<E> list = getBacking();
		return list.listIterator();
	}


	@Override
	public ListIterator<E> listIterator(int index)
	{
		final List<E> list = getBacking();
		return list.listIterator(index);
	}


	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		final List<E> list = getBacking();
		return list.subList(fromIndex, toIndex);
	}


	@Override
	public void sort(Comparator<? super E> c)
	{
		final List<E> list = getBacking();
		Collections.sort(list, c);
	}


	public int binarySearch(E key, Comparator<? super E> c)
	{
		final List<E> list = getBacking();
		return Collections.binarySearch(list, key, c);
	}


	public <K> int binarySearch(K key, Function<E, ? extends Comparable<K>> mapper)
	{
		if (this.mCollection instanceof RandomAccess || size() < BINARYSEARCH_THRESHOLD)
			return indexedBinarySearch(this, key, mapper);
		return iteratorBinarySearch(this, key, mapper);
	}


	/**
	 * See Collections#indexedBinarySearch(List, Object).
	 */
	private <K> int indexedBinarySearch(List<? extends E> list, K key, Function<E, ? extends Comparable<K>> mapper)
	{
		int low = 0;
		int high = list.size() - 1;

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			Comparable<? super K> midVal = mapper.apply(list.get(mid));
			int cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}


	/**
	 * See Collections#iteratorBinarySearch(List, Object).
	 */
	private <K> int iteratorBinarySearch(List<? extends E> list, K key, Function<E, ? extends Comparable<K>> mapper)
	{
		int low = 0;
		int high = list.size() - 1;
		ListIterator<? extends E> i = list.listIterator();

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			Comparable<? super K> midVal = get(i, mid, mapper);
			int cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}


	/**
	 * See Collections#get(ListIterator, int).
	 */
	private <K> K get(ListIterator<? extends E> i, int index, Function<E, K> mapper)
	{
		K obj = null;
		int pos = i.nextIndex();
		if (pos <= index)
		{
			do
			{
				obj = mapper.apply(i.next());
			} while (pos++ < index);
		}
		else
		{
			do
			{
				obj = mapper.apply(i.previous());
			} while (--pos > index);
		}
		return obj;
	}


	public <K> int binarySearch(K key, Comparator<? super K> c, Function<E, K> mapper)
	{
		if (c == null)
			return binarySearch(key, (Function) mapper);

		if (this.mCollection instanceof RandomAccess || size() < BINARYSEARCH_THRESHOLD)
			return indexedBinarySearch(this, key, c, mapper);
		return iteratorBinarySearch(this, key, c, mapper);
	}


	/**
	 * See Collections#indexedBinarySearch(List, Object, Comparator).
	 */
	private <K> int indexedBinarySearch(List<? extends E> l, K key, Comparator<? super K> c, Function<E, K> mapper)
	{
		int low = 0;
		int high = l.size() - 1;

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			K midVal = mapper.apply(l.get(mid));
			int cmp = c.compare(midVal, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}


	/**
	 * See Collections#iteratorBinarySearch(List, Object, Comparator).
	 */
	private <K> int iteratorBinarySearch(List<? extends E> l, K key, Comparator<? super K> c, Function<E, K> mapper)
	{
		int low = 0;
		int high = l.size() - 1;
		ListIterator<? extends E> i = l.listIterator();

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			K midVal = get(i, mid, mapper);
			int cmp = c.compare(midVal, key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found
	}


	public void reverse()
	{
		final List<E> list = getBacking();
		Collections.reverse(list);
	}


	public void shuffle()
	{
		final List<E> list = getBacking();
		Collections.shuffle(list);
	}


	public void shuffle(Random rnd)
	{
		final List<E> list = getBacking();
		Collections.shuffle(list, rnd);
	}


	public void swap(int i, int j)
	{
		final List<E> list = getBacking();
		Collections.swap(list, i, j);
	}


	public void fill(E obj)
	{
		final List<E> list = getBacking();
		Collections.fill(list, obj);
	}


	public void copy(List<? extends E> src)
	{
		final List<E> list = getBacking();
		Collections.copy(list, src);
	}


	public void rotate(int distance)
	{
		final List<E> list = getBacking();
		Collections.rotate(list, distance);
	}


	public boolean replaceAll(E oldVal, E newVal)
	{
		final List<E> list = getBacking();
		return Collections.replaceAll(list, oldVal, newVal);
	}


	public int indexOfSubList(List<?> target)
	{
		final List<E> list = getBacking();
		return Collections.indexOfSubList(list, target);
	}


	public int lastIndexOfSubList(List<?> target)
	{
		final List<E> list = getBacking();
		return Collections.lastIndexOfSubList(list, target);
	}


	public ListExt<E> synchronizedList()
	{
		final List<E> list = getBacking();
		return new ListExt<>(Collections.synchronizedList(list));
	}


	public ListExt<E> unmodifiableList()
	{
		final List<E> list = getBacking();
		return new ListExt<>(Collections.unmodifiableList(list));
	}


	public ListExt<E> checkedList(Class<E> type)
	{
		final List<E> list = getBacking();
		return new ListExt<>(Collections.checkedList(list, type));
	}


	/**
	 * Returns an iterable whose iterator traverses this list in reverse order.
	 * <p>
	 * Code inspired by: <a
	 * href="http://stackoverflow.com/questions/1098117/can-one-do-a-for-each-loop-in-java-in-reverse-order" >Stack
	 * Overflow: Can one do a for each loop in java in reverse order?</a>
	 * </p>
	 * 
	 * @return a reverse {@code Iterable} instance
	 */
	public Iterable<E> reverseIterable()
	{
		return new Iterable<E>()
			{

				@Override
				public Iterator<E> iterator()
				{
					final ListIterator<E> i = listIterator(size());
					return new Iterator<E>()
						{
							@Override
							public boolean hasNext()
							{
								return i.hasPrevious();
							}


							@Override
							public E next()
							{
								return i.previous();
							}


							@Override
							public void remove()
							{
								i.remove();
							}
						};
				}

			};
	}


	public Iterator<E> reverseIterator()
	{
		return reverseIterable().iterator();
	}
}
