package grammar;

import java.util.ArrayList;
import java.util.List;

public class BoolArray extends Array<Bool> {

	protected BoolArray(List<Bool> values) {
		super(values);
	}

	public BoolArray(Bool value) {
		super(new ArrayList<>());
		this.inner.add(value);
	}

	@Override
	protected Array<Bool> makeNew() {
		return new BoolArray(new ArrayList<>());
	}

	@Override
	public Array<Bool> clone() {
		return new BoolArray(new ArrayList<>(this.inner));
	}

	@Override
	public BoolArray slice(Int start) {
		return (BoolArray) super.slice(start);
	}

	@Override
	public BoolArray slice(Int start, Int end) {
		return (BoolArray) super.slice(start, end);
	}

	@Override
	public BoolArray sort() {
		return (BoolArray) super.sort();
	}

	@Override
	public BoolArray reverse() {
		return (BoolArray) super.reverse();
	}

	@Override
	public BoolArray splice(Int start, Int end) {
		return (BoolArray) super.splice(start, end);
	}

	@Override
	public BoolArray splice(Int start) {
		return (BoolArray) super.splice(start);
	}

	public BoolArray concat(BoolArray other) {
		return (BoolArray) super.concat(other);
	}
}
