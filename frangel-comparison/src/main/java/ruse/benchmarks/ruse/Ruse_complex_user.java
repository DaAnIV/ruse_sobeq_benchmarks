package ruse.benchmarks.ruse;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.User;
import ruse.classes.ruse.ComplexUser;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_complex_user implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_complex_user")
                .setInputTypes(User.class, User.class, ComplexUser.class)
                .setInputNames("a", "b", "c")
                .setOutputType(String.class)
                .addPackages("ruse.classes.ruse")
                .addLiterals(String.class, " ")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> {
                    ComplexUser c = new ComplexUser(new User("John", "Doe"), new User("Alex", "Man"));
                    return new Object[] { c.user_a, c.user_b, c };
                })
                .setOutput("John Alex"));

        task.addExample(new Example()
                .setInputs(() -> { 
                    ComplexUser c = new ComplexUser(new User("Alan", "Wake"), new User("Kevin", "Duck"));
                    return new Object[] { c.user_a, c.user_b, c };
                })
                .setOutput("Alan Kevin"));

        return task;
    }

    public static String solution(User a, User b, ComplexUser c) {
        return a.name + " " + b.name;
    }
}
