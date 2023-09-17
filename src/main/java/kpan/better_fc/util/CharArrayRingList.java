package kpan.better_fc.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class CharArrayRingList implements Deque<Character>, List<Character> {
	private final List<Character> delegate;//TODO:自前実装

	public CharArrayRingList() { this(new ArrayList<>()); }
	public CharArrayRingList(int initialCapacity) { this(new ArrayList<>(initialCapacity)); }
	public CharArrayRingList(Collection<Character> c) { this(new ArrayList<>(c)); }
	private CharArrayRingList(List<Character> delegate) { this.delegate = delegate; }
	@Override
	public void addFirst(Character character) {
		delegate.add(0, character);
	}
	@Override
	public void addLast(Character character) {
		delegate.add(character);
	}
	@Override
	public boolean offerFirst(Character character) {
		addFirst(character);
		return true;
	}
	@Override
	public boolean offerLast(Character character) {
		addLast(character);
		return true;
	}
	@Override
	public Character removeFirst() {
		Character c = pollFirst();
		if (c == null)
			throw new NoSuchElementException();
		return c;
	}
	@Override
	public Character removeLast() {
		Character c = pollLast();
		if (c == null)
			throw new NoSuchElementException();
		return c;
	}
	@Override
	public Character pollFirst() {
		if (delegate.isEmpty())
			return null;
		return delegate.remove(0);
	}
	@Override
	public Character pollLast() {
		if (delegate.isEmpty())
			return null;
		return delegate.remove(delegate.size() - 1);
	}
	@Override
	public Character getFirst() {
		Character c = peekFirst();
		if (c == null)
			throw new NoSuchElementException();
		return c;
	}
	@Override
	public Character getLast() {
		Character c = peekLast();
		if (c == null)
			throw new NoSuchElementException();
		return c;
	}
	@Override
	public Character peekFirst() {
		if (delegate.isEmpty())
			return null;
		return delegate.get(0);
	}
	@Override
	public Character peekLast() {
		if (delegate.isEmpty())
			return null;
		return delegate.get(delegate.size() - 1);
	}
	@Override
	public boolean removeFirstOccurrence(Object o) {
		return delegate.remove(o);
	}
	@Override
	public boolean removeLastOccurrence(Object o) {
		for (int i = delegate.size() - 1; i >= 0; i--) {
			if (delegate.get(i).equals(o)) {
				delegate.remove(i);
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean add(Character character) {
		return offerFirst(character);
	}
	@Override
	public boolean offer(Character character) {
		return offerFirst(character);
	}
	@Override
	public Character remove() {
		return removeFirst();
	}
	@Override
	public Character poll() {
		return pollFirst();
	}
	@Override
	public Character element() {
		return getFirst();
	}
	@Override
	public Character peek() {
		return peekFirst();
	}
	@Override
	public boolean addAll(Collection<? extends Character> c) {
		return delegate.addAll(c);
	}
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return delegate.removeAll(c);
	}
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return delegate.retainAll(c);
	}
	@Override
	public void clear() {
		delegate.clear();
	}
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends Character> c) {
		return delegate.addAll(index, c);
	}
	@Override
	public Character get(int index) {
		return delegate.get(index);
	}
	@Override
	public Character set(int index, Character element) {
		return delegate.set(index, element);
	}
	@Override
	public void add(int index, Character element) {
		delegate.add(index, element);
	}
	@Override
	public Character remove(int index) {
		return delegate.remove(index);
	}
	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}
	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}
	@NotNull
	@Override
	public ListIterator<Character> listIterator() {
		return delegate.listIterator();
	}
	@NotNull
	@Override
	public ListIterator<Character> listIterator(int index) {
		return delegate.listIterator(index);
	}
	@NotNull
	@Override
	public List<Character> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}
	@Override
	public void push(Character character) {
		addFirst(character);
	}
	@Override
	public Character pop() {
		return removeFirst();
	}
	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return delegate.containsAll(c);
	}
	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}
	@Override
	public int size() {
		return delegate.size();
	}
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	@Override
	public Iterator<Character> iterator() {
		return delegate.iterator();
	}
	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}
	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
	@Override
	public Iterator<Character> descendingIterator() {
		final ListIterator<Character> li = delegate.listIterator(delegate.size());
		return new Iterator<>() {
			@Override
			public boolean hasNext() { return li.hasPrevious(); }
			@Override
			public Character next() { return li.previous(); }
		};
	}
}
