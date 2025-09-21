package ruse.classes.linear;

import ruse.classes.utils.Utils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;

import com.opengamma.analytics.math.linearalgebra.SVDecompositionCommonsResult;


public class SingularValueDecomposition {
    private int m;
    private int n;
    private double[] s;
    private Matrix U;
    private Matrix V;

    public SingularValueDecomposition(Matrix value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Matrix must be non-empty");
        }

        int m = value.rows;
        int n = value.columns;
        
        Array2DRowRealMatrix v1 = new Array2DRowRealMatrix(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                v1.setEntry(i, j, value.get(i, j));
            }
        }
        org.apache.commons.math.linear.SingularValueDecomposition v2 = new org.apache.commons.math.linear.SingularValueDecompositionImpl(v1);
        SVDecompositionCommonsResult v3 = new SVDecompositionCommonsResult(v2);
        

        double[][] UMatRes = v3.getU().toArray();
        double[][] SMatRes = v3.getS().toArray();
        double[][] VMatRes = v3.getV().toArray();

        U = new Matrix(UMatRes);
        V = new Matrix(VMatRes);
        s = new double[SMatRes.length];
        for (int i = 0; i < SMatRes.length; i++) {
            s[i] = SMatRes[i][i];
        }
    }

    /**
     * Get the inverse of the matrix using SVD
     */
    public Matrix inverse() {
        Matrix V = this.V;
        double e = this.getThreshold();
        int vrows = V.rows;
        int vcols = V.columns;
        Matrix X = Matrix.zeros(vrows, this.s.length);
         
        for (int i = 0; i < vrows; i++) {
            for (int j = 0; j < vcols; j++) {
                if (Math.abs(this.s[j]) > e) {
                    X.set(i, j, V.get(i, j) / this.s[j]);
                }
            }
        }
 
        Matrix U = this.U;
        int urows = U.rows;
        int ucols = U.columns;
        Matrix Y = Matrix.zeros(vrows, urows);

        for (int i = 0; i < vrows; i++) {
            for (int j = 0; j < urows; j++) {
                double sum = 0;
                for (int k = 0; k < ucols; k++) {
                    sum += X.get(i, k) * U.get(j, k);
                }
                Y.set(i, j, sum);
            }
        }
        return Y;
    }

    /**
     * Solve a problem of least square (Ax=b) by using the SVD
     */
    public Matrix solve(Matrix value) {
        Matrix Y = value;
        double e = this.getThreshold();
        int scols = this.s.length;
        Matrix Ls = Matrix.zeros(scols, scols);

        for (int i = 0; i < scols; i++) {
            if (Math.abs(this.s[i]) <= e) {
                Ls.set(i, i, 0);
            } else {
                Ls.set(i, i, 1 / this.s[i]);
            }
        }

        Matrix U = this.U;
        Matrix V = this.getRightSingularVectors();

        Matrix VL = V.mmul(Ls);
        int vrows = V.rows;
        int urows = U.rows;
        Matrix VLU = Matrix.zeros(vrows, urows);

        for (int i = 0; i < vrows; i++) {
            for (int j = 0; j < urows; j++) {
                double sum = 0;
                for (int k = 0; k < scols; k++) {
                    sum += VL.get(i, k) * U.get(j, k);
                }
                VLU.set(i, j, sum);
            }
        }

        return VLU.mmul(Y);
    }

    public Matrix solveForDiagonal(double[] value) {
        return this.solve(Matrix.diag(value));
    }

    public double getCondition() {
        return this.s[0] / this.s[Math.min(this.m, this.n) - 1];
    }

    public double getNorm2() {
        return this.s[0];
    }

    public int getRank() {
        double tol = Math.max(this.m, this.n) * this.s[0] * Double.MIN_VALUE;
        int r = 0;
        for (int i = 0; i < s.length; i++) {
            if (s[i] > tol) {
                r++;
            }
        }
        return r;
    }

    public double[] getDiagonal() {
        return this.s.clone();
    }

    public double getThreshold() {
        return (Double.MIN_VALUE / 2) * Math.max(this.m, this.n) * this.s[0];
    }

    public Matrix getLeftSingularVectors() {
        return this.U;
    }

    public Matrix getRightSingularVectors() {
        return this.V;
    }

    public Matrix getDiagonalMatrix() {
        return Matrix.diag(this.s);
    }
}