package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.User;
import ruse.classes.ruse.UserTuple;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_user_names implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_user_names")
                .setInputTypes(User.class, User.class, UserTuple.class)
                .setInputNames("a", "b", "c")
                .setOutputType(String.class)
                .addLiterals(String.class, ", ")
                .addClasses(User.class, UserTuple.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                    new User("John", "Doe"),
                    new User("Alex", "Man"),
                    new UserTuple(new User[] { new User("John", "Doe"), new User("Alex", "Man") })
                })
                .setOutput("John, Alex"));

        task.addExample(new Example()
                .setInputs(() -> new Object[] { 
                    new User("Alan", "Wake"),
                    new User("Kevin", "Duck"),
                    new UserTuple(new User[] { new User("Alan", "Wake"), new User("Kevin", "Duck") })
                })
                .setOutput("Alan, Kevin"));

        return task;
    }

    public static String solution(User a, User b, UserTuple c) {
        return a.name + ", " + b.name;
    }
}
