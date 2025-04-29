import grammar.Int;
import grammar.IntArray;
import grammar.Str;
import grammar.StrArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class GrammarTests {
	// ---
	// IntArray tests
	// ---
	@Test
	void arraySlice() {
		IntArray l = toArray(1, 2, 3);
		IntArray sliceZero = l.slice(new Int(0));
		assertEquals(sliceZero, l);
		assertNotSame(l, sliceZero);

		IntArray sliceNegOne = l.slice(new Int(-1));
		assertEquals(sliceNegOne, toArray(3));

		IntArray s2 = l.slice(new Int(-2));
		assertEquals(s2, toArray(2, 3));

		IntArray l2 = new IntArray(new ArrayList<>());
		IntArray sliceZero2 = l2.slice(new Int(0));
		assertEquals(sliceZero2, l2);
		assertNotSame(l2, sliceZero2);

		IntArray sliceNegOne2 = l2.slice(new Int(-1));
		assertEquals(sliceNegOne2, l2);
		assertNotSame(l2, sliceNegOne2);
	}

	@Test
	void arrayBinSlice() {
		IntArray l = toArray(1, 2, 3);

		IntArray s1 = l.slice(new Int(1), new Int(2));
		assertEquals(s1, toArray(2));

		IntArray s2 = l.slice(new Int(1), new Int(10));
		assertEquals(s2, toArray(2, 3));

		IntArray s3 = l.slice(new Int(-3), new Int(-1));
		assertEquals(s3, toArray(1, 2));

		IntArray s4 = l.slice(new Int(-3), new Int(-2));
		assertEquals(s4, toArray(1));

		IntArray sliceZero = l.slice(new Int(0), new Int(-1));
		assertEquals(sliceZero, toArray(1, 2));

		IntArray sliceNegOne = l.slice(new Int(-1), new Int(0));
		assertEquals(sliceNegOne, new IntArray(new ArrayList<>()));

		IntArray l2 = new IntArray(new ArrayList<>());
		IntArray sliceZero2 = l2.slice(new Int(0), new Int(-1));
		assertEquals(sliceZero2, l2);
		assertNotSame(l2, sliceZero2);

		IntArray sliceNegOne2 = l2.slice(new Int(-1), new Int(0));
		assertEquals(sliceNegOne2, l2);
		assertNotSame(l2, sliceNegOne2);
	}

	@Test
	void arraySplice() {
		IntArray l = toArray(1, 2, 3);
		IntArray s1 = l.splice(new Int(1));
		assertEquals(toArray(1), l);
		assertEquals(toArray(2, 3), s1);

		l = toArray(1, 2, 3);
		IntArray s2 = l.splice(new Int(0));
		assertEquals(new IntArray(new ArrayList<>()), l);
		assertEquals(toArray(1, 2, 3), s2);

		l = toArray(1, 2, 3);
		IntArray s3 = l.splice(new Int(-1));
		assertEquals(toArray(1, 2), l);
		assertEquals(toArray(3), s3);

		l = toArray(1, 2, 3);
		IntArray s4 = l.splice(new Int(-2));
		assertEquals(toArray(1), l);
		assertEquals(toArray(2, 3), s4);
	}

	@Test
	void arrayTernarySplice() {
		IntArray l = toArray(1, 2, 3);
		IntArray spl = l.splice(new Int(1), new Int(2));
		assertEquals(spl, toArray(2, 3));
		assertEquals(l, toArray(1));

		l = toArray(1, 2, 3);
		IntArray s2 = l.splice(new Int(0), new Int(3));
		assertEquals(new IntArray(new ArrayList<>()), l);
		assertEquals(toArray(1, 2, 3), s2);

		l = toArray(1, 2, 3);
		IntArray s3 = l.splice(new Int(-2), new Int(1));
		assertEquals(toArray(1, 3), l);
		assertEquals(toArray(2), s3);

		l = toArray(1, 2, 3);
		IntArray s4 = l.splice(new Int(-2), new Int(-1));
		assertEquals(toArray(1, 2, 3), l);
		assertEquals(new IntArray(new ArrayList<>()), s4);
	}

	@Test
	void intArraySpliceReplace() {
		//This is a specific case of ternary splice where the middle argument is 1
		IntArray l = toArray(1,2,3);
		IntArray spl = l.spliceReplace(new Int(1), new Int(8));
		assertEquals(spl, toArray(2));
		assertEquals(l,toArray(1,8,3));

		l = toArray(1, 2, 3);
		IntArray s3 = l.spliceReplace(new Int(-2), new Int(12));
		assertEquals(toArray(1, 12, 3), l);
		assertEquals(toArray(2), s3);


		l = toArray(1, 2, 3);
		IntArray s0 = l.spliceReplace(new Int(0), new Int(3));
		assertEquals(toArray(1), s0);
		assertEquals(toArray(3, 2, 3), l);
	}

	// ---
	// Utilities
	// ---
	IntArray toArray(Integer... lst) {
		return new IntArray(Arrays.stream(lst).map(Int::new).collect(Collectors.toList()));
	}

	StrArray toArray(String... lst) {
		return new StrArray(Arrays.stream(lst).map(Str::new).collect(Collectors.toList()));
	}
}
