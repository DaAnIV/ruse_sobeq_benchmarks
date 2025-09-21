package ruse.benchmarks.frangel;

import ruse.classes.linear.*;

import java.util.Arrays;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.benchmarks.RuseBenchmarkUtils;

public enum SyPet_01_invert implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_01_invert")
                .setInputTypes(Matrix.class)
                .setInputNames("mat")
                .setOutputType(Matrix.class)
                .addPackages("ruse.classes.linear")
                .addEqualityTester(Matrix.class, RuseBenchmarkUtils::equalsMatrix)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{1,2,3},{4,5,6}}) })
                .setOutput(new Matrix(new double[][]{{-0.944444,0.444444},{-0.111111,0.111111},{0.722222,-0.222222}})));

        return task;
    }

    public static Matrix solution(Matrix arg0) {
        SingularValueDecomposition v1 = new SingularValueDecomposition(arg0);
        Matrix v2 = v1.inverse();
        return v2;
    }
}
