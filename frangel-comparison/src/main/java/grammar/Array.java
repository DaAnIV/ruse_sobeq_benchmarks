package grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class Array<T extends Comparable<T>> extends SobeqValue<List<T>> {

	protected Array(List<T> inner) {
		super(inner);
	}

	protected abstract Array<T> makeNew();

	public abstract Array<T> clone();

	public T deref(Int idx) {
		return this.inner.get(idx.inner);
	}

	public Int length() {
		return new Int(this.inner.size());
	}

	public Array<T> slice(Int start) {
		int from = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
		if (from < 0) {
			from = 0;
		}

		final Array<T> rs = this.makeNew();
		rs.inner.addAll(this.inner.subList(from, this.inner.size()));
		return rs;
	}

	public Array<T> slice(Int start, Int end) {
		int from = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
		if (from >= this.inner.size()) {
			from = this.inner.size();
		} else if (from < 0) {
			from = 0;
		}

		int until = end.inner >= 0 ? end.inner : this.inner.size() + end.inner;
		if (until >= this.inner.size()) {
			until = this.inner.size();
		} else if (until < 0) {
			until = 0;
		}
		if (until < from) {
			until = from;
		}


		final Array<T> rs = this.makeNew();
		rs.inner.addAll(this.inner.subList(from, until));
		return rs;
	}

	public Array<T> concat(Array<T> other) {
		if (other.inner.isEmpty()) {
			return this.clone();
		}

		if (this.inner.isEmpty()) {
			return other.clone();
		}

		// Type check!
		if (this.inner.get(0).getClass() != other.inner.get(0).getClass()) {
			throw new IllegalArgumentException();
		}

		final Array<T> rs = this.makeNew();
		rs.inner.addAll(this.inner);
		rs.inner.addAll(other.inner);
		return rs;
	}

	public Str join(final Str s) {
		return new Str(this.inner
			.stream()
			.map(Object::toString)
			.collect(Collectors.joining(s.inner)));
	}
	
	public Int push(T val) {
		this.inner.add(val);
		return new Int(this.inner.size());
	}

	public Array<T> sort() {
		Collections.sort(this.inner);
		return this;
	}

	public Array<T> reverse() {
		Collections.reverse(this.inner);
		return this;
	}

//	public Array<T> splice(Int start, Int end, T val) {
//		int from = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
//		int count = end.inner >= 0 ? end.inner : 0;
//
//		final Array<T> rs = this.makeNew();
//		rs.inner.addAll(this.inner.subList(from, from + count));
//
//		final List<T> newInner = new ArrayList<>(this.inner.size() - count + 1);
//		newInner.addAll(this.inner.subList(0, from));
//		newInner.add(val);
//		newInner.addAll(this.inner.subList(from + count, this.inner.size()));
//		this.inner = newInner;
//
//		return rs;
//	}

	public Array<T> splice(Int start, Int end) {
		int from = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
		int count = end.inner >= 0 ? end.inner : 0;

		final Array<T> rs = this.makeNew();
		rs.inner.addAll(this.inner.subList(from, from + count));

		final List<T> newInner = new ArrayList<>(this.inner.size() - count);
		newInner.addAll(this.inner.subList(0, from));
		newInner.addAll(this.inner.subList(from + count, this.inner.size()));
		this.inner = newInner;

		return rs;
	}

	public Array<T> splice(Int start) {
		int from = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
		final Array<T> rs = this.makeNew();
		rs.inner.addAll(this.inner.subList(from, this.inner.size()));
		this.inner = this.inner.subList(0, from);
		return rs;
	}


	public T pop() {
		return this.inner.remove(this.inner.size() - 1);
	}

	public T shift() {
		return this.inner.remove(0);
	}

}
