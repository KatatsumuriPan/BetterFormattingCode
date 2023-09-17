package kpan.better_fc.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListUtil {
	public static <T> Iterator<T> descendingIteratorOf(List<T> list) {
		return new Iterator<>() {

			private final ListIterator<T> itr = list.listIterator(list.size());

			@Override
			public boolean hasNext() {
				return itr.hasPrevious();
			}

			@Override
			public T next() {
				return itr.previous();
			}
		};
	}
}
