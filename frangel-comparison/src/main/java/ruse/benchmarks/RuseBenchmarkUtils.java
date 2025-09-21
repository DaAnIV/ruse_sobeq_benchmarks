package ruse.benchmarks;

import java.util.Arrays;

import ruse.classes.linear.Matrix;
import ruse.classes.polynomial.Complex;
import frangel.benchmarks.BenchmarkUtils;

public class RuseBenchmarkUtils {

    public static boolean equalsDoubleArray(double[] array1, double[] array2) {
        if (array1 == null || array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!BenchmarkUtils.equalsDouble(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean equalsMatrix(Matrix matrix1, Matrix matrix2) {
        if (matrix1 == null || matrix2 == null) {
            return false;
        }
        if (matrix1.rows != matrix2.rows || matrix1.columns != matrix2.columns) {
            return false;
        }
        for (int i = 0; i < matrix1.rows; i++) {
            for (int j = 0; j < matrix1.columns; j++) {
                if (!BenchmarkUtils.equalsDouble(matrix1.get(i, j), matrix2.get(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean checkComplexArray(Complex[] comp, Complex[] expected) {
        return checkComplexArray(comp, expected, 1e-3);
    }
    
    public static boolean checkComplexArray(Complex[] comp, Complex[] expected, double epsilon) {
        if (comp == null || expected == null) {
            return false;
        }
        if (comp.length != expected.length) {
            return false;
        }
        Arrays.sort(comp, (a, b) -> Double.compare(a.imag, b.imag));
        Arrays.sort(comp, (a, b) -> Double.compare(a.real, b.real));
        Arrays.sort(expected, (a, b) -> Double.compare(a.imag, b.imag));
        Arrays.sort(expected, (a, b) -> Double.compare(a.real, b.real));
        for (int i = 0; i < comp.length; i++) {
            if (Math.abs(comp[i].real - expected[i].real) > epsilon || Math.abs(comp[i].imag - expected[i].imag) > epsilon) {
                return false;
            }
        }
        return true;
    }
}