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
 *  Advanced Tactical Simulation Team (Code 5774)
 *  4555 Overlook Ave SW
 *  Washington, DC 20375
 *
 *  For more information call (202)767-2897 or send email to
 *  BuilderSupport@nrl.navy.mil.
 *
 *  The U.S. Government retains all rights to use, duplicate,
 *  distribute, disclose, or release this software.
 */

package builder.mil.nrl.spg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import builder.mil.nrl.atest.util.ListExt;

/**
 * Base class for all <code>List</code> decorators.
 * <p>
 * Any method calls to this class are forwarded to the underlying list.
 * </p>
 * 
 * @author mamaril
 * @param <E> element type stored by this list
 * @since May 22, 2007
 */
public abstract class ListDecorator<E> implements List<E>
{
	protected abstract class ListIteratorDecorator implements ListIterator<E>
	{
		/**
		 * The underlying iterator for this list iterator decorator
		 */
		private final ListIterator<E> mIterator;


		/**
		 * Constructs a list iterator decorator built upon the specified <code>it</code>
		 * 
		 * @param it the underlying iterator for this list iterator decorator
		 * @throws NullPointerException if <code>it</code> is <code>null</code>
		 */
		protected ListIteratorDecorator(final ListIterator<E> it)
		{
			if (it == null)
			{
				throw new NullPointerException("iterator cannot be null");
			}
			mIterator = it;
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		@Override
		public void add(final E o)
		{
			mIterator.add(o);
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return mIterator.hasNext();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious()
		{
			return mIterator.hasPrevious();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#next()
		 */
		@Override
		public E next()
		{
			return mIterator.next();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex()
		{
			return mIterator.nextIndex();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#previous()
		 */
		@Override
		public E previous()
		{
			return mIterator.previous();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex()
		{
			return mIterator.previousIndex();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#remove()
		 */
		@Override
		public void remove()
		{
			mIterator.remove();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		@Override
		public void set(final E o)
		{
			mIterator.set(o);
		}

	}

	/**
	 * The underlying list for this list decorator
	 */
	private final ListExt<E> mList;


	/**
	 * Constructs a list decorator whose underlying list is a non-null empty <code>list</code>.
	 */
	public ListDecorator()
	{
		this(null);
	}


	/**
	 * Constructs a list decorator built upon the specified <code>list</code>
	 * <p>
	 * If <code>list</code> is <code>null</code>, an empty list is created as the backing.
	 * </p>
	 * 
	 * @param list the underlying list
	 */
	public ListDecorator(final List<E> list)
	{
		mList = new ListExt<>((list == null) ? new ArrayList<E>() : list);
	}


	protected ListExt<E> getBacking()
	{
		return mList;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(final E o)
	{
		return mList.add(o);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(final int index, final E element)
	{
		mList.add(index, element);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		return mList.addAll(c);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c)
	{
		return mList.addAll(index, c);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear()
	{
		mList.clear();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object elem)
	{
		return mList.contains(elem);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return mList.containsAll(c);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o)
	{
		return mList.equals(o);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public E get(final int index)
	{
		return mList.get(index);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return mList.hashCode();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(final Object elem)
	{
		return mList.indexOf(elem);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return mList.isEmpty();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<E> iterator()
	{
		return mList.iterator();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(final Object elem)
	{
		return mList.lastIndexOf(elem);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<E> listIterator()
	{
		return mList.listIterator();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(final int index)
	{
		return mList.listIterator(index);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public E remove(final int index)
	{
		return mList.remove(index);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o)
	{
		return mList.remove(o);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return mList.removeAll(c);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return mList.retainAll(c);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public E set(final int index, final E element)
	{
		return mList.set(index, element);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size()
	{
		return mList.size();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<E> subList(final int fromIndex, final int toIndex)
	{
		return mList.subList(fromIndex, toIndex);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		return mList.toArray();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] a)
	{
		return mList.toArray(a);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return mList.toString();
	}

}
