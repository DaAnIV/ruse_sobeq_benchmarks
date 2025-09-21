package ruse.benchmarks.frangel;

import java.util.Arrays;

import frangel.Example;
import frangel.SynthesisTask;
import frangel.Tag;
import frangel.benchmarks.TaskCreator;

import ruse.benchmarks.RuseBenchmarkGroup;
import ruse.classes.linear.Matrix;
import ruse.classes.linear.SingularValueDecomposition;

public enum SyPet_04_evaluate implements TaskCreator {
    INSTANCE;
    static {
        RuseBenchmarkGroup.FRANGEL.register(INSTANCE);
    }

    @Override
    public SynthesisTask createTask() {
        SynthesisTask task = new SynthesisTask()
                .setName("sypet_04_evaluate")
                .setInputTypes(Matrix.class)
                .setInputNames("mat")
                .setOutputType(SingularValueDecomposition.class)
                .addPackages("ruse.classes.linear")
                .addTags(Tag.SINGLE_LINE); // Easily written in one line

        task.addExample(new Example()
                .setInputs(() -> new Object[] { new Matrix(new double[][]{{1,2},{2,2},{2,1}}) })
                .setOutputChecker((SingularValueDecomposition res2) -> {
                    if (res2 == null)
                        return false;

                    double inv_sqrt2 = 1/Math.sqrt(2);
                    double[][] UMat = new double[][]{{3/Math.sqrt(34),-1/Math.sqrt(2)},{4/Math.sqrt(34),0},{3/Math.sqrt(34),1/Math.sqrt(2)}};
                    double[] SMat = new double[]{Math.sqrt(17),1};
                    double[][] VTMat = new double[][]{{inv_sqrt2,inv_sqrt2},{inv_sqrt2,-inv_sqrt2}};

                    Matrix UMatRes = res2.getLeftSingularVectors();
                    double[] SMatRes = res2.getDiagonal();
                    Matrix VTMatRes = res2.getRightSingularVectors();

                    // System.err.println("UMatRes: " + Arrays.toString(UMatRes.to1DArray()));
                    // System.err.println("SMatRes: " + Arrays.toString(SMatRes));
                    // System.err.println("VTMatRes: " + Arrays.toString(VTMatRes.to1DArray()));
                    // System.err.println("UMat: " + Arrays.toString(UMat));
                    // System.err.println("SMat: " + Arrays.toString(SMat));
                    // System.err.println("VTMat: " + Arrays.toString(VTMat));

                    if(UMat.length != UMatRes.rows ||
                       UMat[0].length != UMatRes.columns ||
                       SMat.length != SMatRes.length ||
                       VTMat[0].length != VTMatRes.columns ||
                       VTMat.length != VTMatRes.rows){
                        return false;
                    }

                    for(int i=0; i<UMat.length; i++){
                        for(int j=0;j<UMat[i].length; j++){
                            if(Math.abs(UMat[i][j]-UMatRes.get(i,j))>0.000005)
                                return false;
                        }
                    }

                    for(int i=0; i<SMat.length; i++){
                        if(Math.abs(SMat[i]-SMatRes[i])>0.000005)
                            return false;
                    }

                    for(int i=0; i<VTMat.length; i++){
                        for(int j=0;j<VTMat[i].length; j++){
                            if(Math.abs(VTMat[i][j]-VTMatRes.get(i,j))>0.000005)
                                return false;
                        }
                    }

                    return true;
                }));

        return task;
    }

    public static SingularValueDecomposition solution(Matrix arg0) {
        return new SingularValueDecomposition(arg0);
    }
}
