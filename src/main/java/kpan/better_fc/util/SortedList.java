package kpan.better_fc.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class SortedList<E> implements Collection<E> {

	private final TreeSet<E> delegate;//TODO:ArrayListを使った実装に変更

	public SortedList(Comparator<? super E> comparator) {
		delegate = new TreeSet<>(comparator);
	}

	@Override
	public int size() { return delegate.size(); }
	@Override
	public boolean isEmpty() { return delegate.isEmpty(); }
	@Override
	public boolean contains(Object o) { return delegate.contains(o); }
	@NotNull
	@Override
	public Iterator<E> iterator() { return delegate.iterator(); }
	@NotNull
	@Override
	public Object[] toArray() { return delegate.toArray(); }
	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] a) { return delegate.toArray(a); }
	@Override
	public boolean add(E e) { return delegate.add(e); }
	@Override
	public boolean remove(Object o) { return delegate.remove(o); }
	@Override
	public boolean containsAll(@NotNull Collection<?> c) { return delegate.containsAll(c); }
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) { return delegate.addAll(c); }
	@Override
	public boolean removeAll(@NotNull Collection<?> c) { return delegate.removeAll(c); }
	@Override
	public boolean retainAll(@NotNull Collection<?> c) { return delegate.retainAll(c); }
	@Override
	public void clear() { delegate.clear(); }
}
