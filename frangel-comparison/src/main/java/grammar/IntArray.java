package grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class IntArray extends Array<Int> {

	public IntArray(List<Int> values) {
		super(values);
	}

	public IntArray(Int value) {
		super(new ArrayList<>());
		this.inner.add(value);
	}

	@Override
	protected IntArray makeNew() {
		return new IntArray(new ArrayList<>());
	}

	@Override
	public IntArray clone() {
		return new IntArray(new ArrayList<>(this.inner));
	}

	@Override
	public IntArray slice(Int start) {
		return (IntArray) super.slice(start);
	}

	@Override
	public IntArray slice(Int start, Int end) {
		return (IntArray) super.slice(start, end);
	}

	@Override
	public IntArray sort() {
		return (IntArray) super.sort();
	}

	@Override
	public IntArray reverse() {
		return (IntArray) super.reverse();
	}

	@Override
	public IntArray splice(Int start, Int end) {
		return (IntArray) super.splice(start, end);
	}

	public IntArray spliceReplace(Int start, Int val) {
		int at = start.inner >= 0 ? start.inner : this.inner.size() + start.inner;
		final Array<Int> rs = this.makeNew();
		rs.inner.add(this.inner.get(at));

		final List<Int> newInner = new ArrayList<>(this.inner);
		newInner.set(at,val);
		this.inner = newInner;

		return (IntArray)rs;
	}

	@Override
	public IntArray splice(Int start) {
		return (IntArray) super.splice(start);
	}

	public IntArray concat(IntArray other) {
		return (IntArray) super.concat(other);
	}
	
	@Override
	public Str join(final Str s) {
		return new Str(this.inner
			.stream()
			.map(item -> String.valueOf(item.inner))
			.collect(Collectors.joining(s.inner)));
	}
}
