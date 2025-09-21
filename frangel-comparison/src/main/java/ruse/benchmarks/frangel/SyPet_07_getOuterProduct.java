package ruse.benchmarks.frangel;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.benchmarks.RuseBenchmarkUtils;
import ruse.classes.linear.Matrix;

public enum SyPet_07_getOuterProduct implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_07_getOuterProduct")
                .setInputTypes(Matrix.class, Matrix.class)
                .setInputNames("vec1", "vec2")
                .setOutputType(Matrix.class)
                .addPackages("ruse.classes.linear")
                .addEqualityTester(Matrix.class, RuseBenchmarkUtils::equalsMatrix)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{1,1,1}}), new Matrix(new double[][]{{1,2,3}}) })
                .setOutput(new Matrix(new double[][] { { 1, 2, 3 }, { 1, 2, 3 }, { 1, 2, 3 } })));

        return task;
    }

    public static Matrix solution(Matrix arg0, Matrix arg1) {
        return arg0.transpose().mmul(arg1);
    }
}
