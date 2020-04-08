/*
 *  =======================================================================
 *  ==                                                                   ==
 *  ==                   Classification: UNCLASSIFIED                    ==
 *  ==                   Classified By:                                  ==
 *  ==                   Declassify On:                                  ==
 *  ==                                                                   ==
 *  =======================================================================
 * 
 * $HeadURL$
 * $Author$
 * $Date$
 * $Id$
 * 
 * Developed by: Naval Research Laboratory, Tactical Electronic Warfare Div.
 * Effectiveness of Navy Electronic Warfare Systems, Code 5707 4555 Overlook
 * Ave. Washington, D.C. 20375-5339
 * 
 * For more information call 202-767-2897 or send email to
 * BuilderSupport@nrl.navy.mil.
 * 
 * The U.S. Government retains all rights to use, duplicate, distribute,
 * disclose, or release this software.
 */

package builder.mil.nrl.spg.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Supplements the methods in {@link java.util.Collections}. Provides methods to search and remove objects from
 * collections based on instance identity, rather than on identity determined by the ".equals" method.
 * 
 * @author lilley
 */
public final class CollectionsExt
{
	/**
	 * Private constructor that prevents developers from creating instances of this class
	 */
	private CollectionsExt()
	{
		// no operation
	}


	public static <T> NavigableSet<T> asSortedSet(Comparator<T> comparator, T firstItem, T... items)
	{
		final TreeSet<T> set = new TreeSet<>(comparator);
		set.add(firstItem);
		if (items != null)
		{
			set.addAll(Arrays.asList(items));
		}
		return set;
	}


	/**
	 * Returns the union of all elements in the specified set list.
	 * 
	 * @param <T>
	 * @param sets
	 * @return Non-null set of parameterized elements.
	 */
	public static <T> Set<T> union(final Collection<Set<T>> sets)
	{
		// declare result
		final Set<T> result = new ArraySet<>();

		// union all sets
		for (final Set<T> set : sets)
		{
			result.addAll(set);
		}

		// done
		return Collections.unmodifiableSet(result);
	}


	/**
	 * Returns those elements that are contained in all sets.
	 * 
	 * @param <T>
	 * @param sets
	 * @return Non-null set of parameterized elements.
	 */
	public static <T> SetExt<T> intersection(final Collection<Set<T>> sets)
	{
		// declare result
		final SetExt<T> result = new SetExt<>(new ArraySet<T>());

		// get union
		result.addAll(union(sets));

		// retain only the elements in all sets
		for (final Set<T> s : sets)
		{
			result.retainAll(s);
		}

		// done
		return result.unmodifiable();
	}


	/**
	 * Returns a list of all elements in the supplied list which are of the supplied type.<br>
	 * A dynamic instance of is performed on each element in the list to see if it is of the requested type.<br>
	 * 
	 * @param <T> the type to search for
	 * @param list the list to search in
	 * @param type the class of the type to search for. typically something like <code>MyResource.class</code>
	 * @return a sublist containing all elements in the list that are of the supplied type; null if the type to look for
	 *         is null; an empty list if there are no elements in the list of the supplied type.
	 * @deprecated Use {@code list.stream().filter(type::isInstance).collect(Collectors.toList())} instead.
	 */
	@Deprecated
	public static <T> List<T> find(Collection<? super T> list, Class<T> type)
	{
		if (type == null)
		{
			return null;
		}

		ArrayList<T> rv = new ArrayList<>(1);

		for (Object o : list)
		{
			if (type.isInstance(o))
			{
				rv.add(type.cast(o));
			}
		}

		rv.trimToSize();
		return rv;
	}


	/**
	 * finds any objects of the specified type
	 * 
	 * @param <T>
	 * @param list
	 * @param type
	 * @return
	 * @deprecated Use {@code list.stream().filter(type::isInstance).collect(Collectors.toList())} instead.
	 */
	@Deprecated
	public static <T> List<T> findAny(Collection<?> list, Class<T> type)
	{
		if (type == null)
		{
			return null;
		}

		ArrayList<T> rv = new ArrayList<>(1);

		for (Object o : list)
		{
			if (type.isInstance(o))
			{
				rv.add(type.cast(o));
			}
		}

		rv.trimToSize();
		return rv;
	}


	/**
	 * Returns the first element of the specified type in the supplied list.<br>
	 * A dynamic instance of is performed on each element in the list to see if it is of the requested type.<br>
	 * 
	 * @param <T> the type to search for
	 * @param list the list to search in
	 * @param type the class of the type to search for. typically something like <code>MyResource.class</code>
	 * @return the first occurance of an element in the list that is of the supplied type; null if the type to look for
	 *         is null; null if there are no elements of the specified type.
	 * @deprecated Use {@code list.stream().filter(type::isInstance).findFirst()} instead.
	 */
	@Deprecated
	public static <T> T findFirst(Collection<? super T> list, Class<T> type)
	{
		if (type == null)
		{
			return null;
		}

		for (Object o : list)
		{
			if (type.isInstance(o))
			{
				return (type.cast(o));
			}
		}

		return null;
	}
}
