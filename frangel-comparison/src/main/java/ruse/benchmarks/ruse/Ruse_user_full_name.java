package ruse.benchmarks.ruse;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.User;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_user_full_name implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_user_full_name")
                .setInputTypes(User.class, User.class)
                .setInputNames("a", "b")
                .addPackages("ruse.classes.ruse")
                .setOutputType(String.class)
                .addLiterals(String.class, " ")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                    new User("John", "Doe"),
                    new User("Alex", "Man")
                })
                .setOutput("John Alex"));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                    new User("Alan", "Wake"),
                    new User("Kevin", "Duck")
                })
                .setOutput("Alan Kevin"));

        return task;
    }

    public static String solution(User a, User b) {
        return a.name + " " + b.name;
    }
}
