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
import java.util.List;
import java.util.ListIterator;

/**
 * List that will go bonkers if a client attempts to add <code>null</code> to it.
 * 
 * @author mamaril
 * @param <E> element type stored by this list
 * @since May 22, 2007
 */
public class NoNullList<E> extends ListDecorator<E>
{
	protected class NoNullListIterator extends ListIteratorDecorator
	{

		protected NoNullListIterator(final ListIterator<E> it)
		{
			super(it);
		}


		@Override
		public void add(final E o)
		{
			if (isNull(o))
			{
				return;
			}
			super.add(o);
		}


		@Override
		public void set(final E o)
		{
			if (isNull(o))
			{
				return;
			}
			super.set(o);
		}

	}

	private final boolean mThrowException;


	public NoNullList()
	{
		this(null, false);
	}


	public NoNullList(final List<E> list)
	{
		this(list, false);
	}


	public NoNullList(final List<E> list, final boolean throwException)
	{
		super(list);

		if (contains(null))
		{
			throw new NullPointerException("null elements not allowed in " + getClass().getCanonicalName());
		}

		mThrowException = throwException;
	}


	@Override
	public boolean add(final E element)
	{
		if (isNull(element))
		{
			return false;
		}
		return super.add(element);
	}


	@Override
	public void add(final int index, final E element)
	{
		if (isNull(element))
		{
			return;
		}
		super.add(index, element);
	}


	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		return super.addAll(purgeNulls(c));
	}


	@Override
	public boolean addAll(final int index, final Collection<? extends E> c)
	{
		return super.addAll(index, purgeNulls(c));
	}


	@Override
	public ListIterator<E> listIterator()
	{
		return new NoNullListIterator(super.listIterator());
	}


	@Override
	public ListIterator<E> listIterator(int index)
	{
		return new NoNullListIterator(super.listIterator(index));
	}


	@Override
	public E set(final int index, final E element)
	{
		if (isNull(element))
		{
			return null;
		}
		return super.set(index, element);
	}


	boolean isNull(final E element)
	{
		if (mThrowException && element == null)
		{
			throw new NullPointerException("null elements not allowed in " + getClass().getCanonicalName());
		}
		return element == null;
	}


	private Collection<? extends E> purgeNulls(final Collection<? extends E> c)
	{
		if (c.contains(null))
		{
			if (mThrowException)
			{
				throw new NullPointerException("null elements not allowed in " + getClass().getCanonicalName());
			}

			List<E> newC = new ArrayList<>();

			// TODO potential for a ConcurrentModificationException

			for (E element : c)
			{
				if (element != null)
				{
					newC.add(element);
				}
			}
			return newC;
		}
		return c;
	}

}
