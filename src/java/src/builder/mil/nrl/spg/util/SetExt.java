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

package builder.mil.nrl.spg.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import builder.mil.nrl.atest.util.CollectionExt;

/**
 * @author doyle
 * @since Apr 25, 2011
 */
public class SetExt<E> extends CollectionExt<E> implements Set<E>
{
	private final static SetExt EMPTY_SET = new SetExt();


	public SetExt(final Collection<? extends E> items)
	{
		super(new HashSet<E>(items));
	}


	public SetExt(Set<E> backing)
	{
		super(backing);
	}


	public SetExt()
	{
		super(new HashSet<E>());
	}


	public static final <T> SetExt<T> empty()
	{
		return EMPTY_SET;
	}


	@Override
	public SetExt<E> unmodifiable()
	{
		return new SetExt<>(Collections.unmodifiableSet(getBacking()));
	}


	private Set<E> getBacking()
	{
		if (mCollection instanceof Set)
		{
			return (Set<E>) mCollection;
		}

		return new HashSet<>(mCollection);
	}


	/**
	 * Returns those elements that are contained in this set and the {@code right} set.
	 * 
	 * @return non-null set of parameterized elements
	 */
	public SetExt<E> intersection(Set<E> right)
	{
		return CollectionsExt.intersection(Arrays.asList(this, right));
	}


	/**
	 * Returns those elements that represent the difference between this set and the {@code right} sets.
	 * 
	 * @return non-null set of parameterized elements
	 */
	public Set<E> difference(final Set<E> right)
	{
		final SetExt<E> result = new SetExt<>(new ArraySet<E>());

		// loop through the left set, add those elements
		// not found in the right set
		for (final E l : this)
		{
			if (!right.contains(l))
			{
				result.add(l);
			}
		}

		// do the same for the right set
		for (final E r : right)
		{
			if (!contains(r))
			{
				result.add(r);
			}
		}

		return Collections.unmodifiableSet(result);
	}
}
