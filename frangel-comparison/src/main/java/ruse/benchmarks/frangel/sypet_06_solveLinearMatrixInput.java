package ruse.benchmarks.frangel;

import java.util.Arrays;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.benchmarks.RuseBenchmarkUtils;
import ruse.classes.linear.Matrix;
import ruse.classes.linear.SingularValueDecomposition;

public enum sypet_06_solveLinearMatrixInput implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_06_solveLinearMatrixInput")
                .setInputTypes(Matrix.class, Matrix.class)
                .setInputNames("mat", "vec")
                .setOutputType(double[].class)
                .addPackages("ruse.classes.linear")
                .addEqualityTester(Double[].class, RuseBenchmarkUtils::equalsDoubleArray)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{3,-1},{1,1}}), Matrix.rowVector(new double[]{3,5}) })
                .setOutput(new double[]{2, 3}));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{1,2,1},{2,-1,3},{3,1,2}}), Matrix.rowVector(new double[]{7,7,18}) })
                .setOutput(new double[]{7, 1, -2}));

        return task;
    }

    public static double[] solution(Matrix mat, Matrix vec) {
        Matrix v2 = vec.transpose();
        SingularValueDecomposition v3 = new SingularValueDecomposition(mat);
        Matrix v5 = v3.inverse();
        double[] v6 = v5.mmul(v2).to1DArray();
        return v6;
    }
}
