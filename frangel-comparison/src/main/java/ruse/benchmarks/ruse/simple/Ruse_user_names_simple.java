package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.User;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_user_names_simple implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_user_names_simple")
                .setInputTypes(User.class, User.class)
                .setInputNames("a", "b")
                .setOutputType(String.class)
                .addLiterals(String.class, ", ")
                .addClasses(User.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> {
                    User user_a = new User("John", "Doe");
                    User user_b = new User("Alex", "Man");
                    return new Object[] { 
                        user_a,
                        user_b,
                    };
                })
                .setOutput("John, Alex"));

        task.addExample(new Example()
                .setInputs(() -> {
                    User user_a = new User("Alan", "Wake");
                    User user_b = new User("Kevin", "Duck");
                    return new Object[] { 
                        user_a,
                        user_b,
                    };
                })
                .setOutput("Alan, Kevin"));

        return task;
    }

    public static String solution(User a, User b) {
        return a.name + ", " + b.name;
    }
}
