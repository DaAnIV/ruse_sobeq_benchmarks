package ruse.benchmarks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.benchmarks.TaskCreator;

public enum RuseBenchmarkGroup {
    FRANGEL,
    RUSE;

    private static final Set<TaskCreator> ALL_CREATORS = new HashSet<>();
    private final List<TaskCreator> creators = new ArrayList<>();

    static {
        Class<?> baseCreator = TaskCreator.class;
        try {
            for (final ClassInfo info : ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive("ruse.benchmarks")) {
                Class<?> cls = info.load();
                if (baseCreator.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers()))
                    Class.forName(cls.getName());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static SynthesisTask getTask(String name) {
        for (TaskCreator creator : ALL_CREATORS) {
            SynthesisTask task = creator.createTask();
            if (task.getName().equals(name))
                return task;
        }
        return null;
    }

    public static SynthesisTask getTaskByCreator(String creatorName) {
        for (TaskCreator creator : ALL_CREATORS) {
            if (creator.getClass().getSimpleName().equals(creatorName)) {
                return creator.createTask();
            }
        }
        return null;
    }

    public static Set<TaskCreator> getAllCreators() {
        return ALL_CREATORS;
    }

    public static RuseBenchmarkGroup findGroup(TaskCreator creator) {
        for (RuseBenchmarkGroup group : values())
            if (group.creators.contains(creator))
                return group;
        return null;
    }

    public void register(TaskCreator creator) {
        creators.add(creator);
        // SynthesisTask task = creator.createTask();
        // System.err.println("Creator name: " + creator.getClass().getSimpleName() + ", task name: " + task.getName());
        ALL_CREATORS.add(creator);
    }

    public List<SynthesisTask> getTasks() {
        creators.sort((TaskCreator c1, TaskCreator c2) -> c1.getClass().getName().compareTo(c2.getClass().getName()));
        List<SynthesisTask> tasks = new ArrayList<>();
        for (TaskCreator creator : creators) {
            SynthesisTask task = creator.createTask();
            task.setGroup(this.toString());
            tasks.add(task);
        }
        return tasks;
    }

    public List<Class<?>> getCreatorClasses() {
        creators.sort((TaskCreator c1, TaskCreator c2) -> c1.getClass().getName().compareTo(c2.getClass().getName()));
        List<Class<?>> list = new ArrayList<>();
        for (TaskCreator creator : creators)
            list.add(creator.getClass());
        return list;
    }


    private static Object getSolution(TaskCreator creator, SynthesisTask task, Object[] inputs) throws Exception {
        Method solution = creator.getClass().getDeclaredMethod("solution", task.getInputTypes());
        return solution.invoke(null, inputs);
    }

    private static boolean checkTask(TaskCreator creator) throws Exception {
        SynthesisTask task = creator.createTask();
        task.finalizeSetup();
        for (Example example : task.getExamples()) {
            Object[] inputs = example.getInputs();
            Object solution = getSolution(creator, task, inputs);
            if (!example.checkModifiedInputs(inputs))
                return false;
            // System.err.println("Solution: " + solution != null ? solution.getClass().getSimpleName() : "null");
            if (!example.checkOutput(solution))
                return false;
        }

        return true;
    }

    public static boolean verifyTasks() {
        boolean error = false;
        for (TaskCreator creator : ALL_CREATORS) {
            try {
                if (!checkTask(creator))
                    throw new RuntimeException("Task " + creator.getClass().getSimpleName() + " has bad solution");
            } catch (Exception e) {
                System.err.println("Error verifying task " + creator.getClass().getSimpleName() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                error = true;
            }
        }
        return !error;
    }
}
