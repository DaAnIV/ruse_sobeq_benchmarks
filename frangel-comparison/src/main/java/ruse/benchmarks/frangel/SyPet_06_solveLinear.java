package ruse.benchmarks.frangel;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.classes.linear.Matrix;
import ruse.classes.linear.SingularValueDecomposition;

public enum SyPet_06_solveLinear implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_06_solveLinear")
                .setInputTypes(double[][].class, double[].class)
                .setInputNames("mat", "vec")
                .setOutputType(double[].class)
                .addPackages("ruse.classes.linear")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new double[][]{{3,-1},{1,1}}, new double[]{3,5} })
                .setOutput(new double[]{2, 3}));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new double[][]{{1,2,1},{2,-1,3},{3,1,2}}, new double[]{7,7,18} })
                .setOutput(new double[]{7, 1, -2}));

        return task;
    }

    public static double[] solution(double[][] mat, double[] vec) {
        Matrix v1 = Matrix.rowVector(vec);
        Matrix v2 = v1.transpose();
        SingularValueDecomposition v3 = new SingularValueDecomposition(new Matrix(mat));
        Matrix v5 = v3.inverse();
        double[] v6 = v5.mmul(v2).to1DArray();
        return v6;
    }
}
