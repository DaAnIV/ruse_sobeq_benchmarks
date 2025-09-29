package ruse.benchmarks.ruse.simple;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.classes.ruse.GraphNode;

import ruse.benchmarks.RuseBenchmarkGroup;

public enum Ruse_graph_one_way_connected implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("ruse_graph_one_way_connected")
                .setInputTypes(GraphNode.class, GraphNode.class, GraphNode.class)
                .setInputNames("graph1", "graph2", "graph3")
                .addClasses(GraphNode.class)
                .setOutputType(int.class)
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> {
                    GraphNode graph3 = new GraphNode(7, new GraphNode(5, new GraphNode(6, new GraphNode(7))));
                    GraphNode graph1 = graph3.neighbors.get(0);
                    GraphNode graph2 = graph1.neighbors.get(0);
                    return new Object[] { 
                        graph1,
                        graph2,
                        graph3
                    };
                })
                .setOutput(18));

        return task;
    }

    public static int solution(GraphNode graph1, GraphNode graph2, GraphNode graph3) {
        return graph1.value + graph2.value + graph3.value;
    }
}
