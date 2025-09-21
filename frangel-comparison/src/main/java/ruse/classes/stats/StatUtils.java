package ruse.classes.stats;

import java.util.Arrays;

/**
 * Determine the coefficient of determination (r^2) of a fit from the observations
 * and predictions.
 */
class StatUtils {
    public static double determinationCoefficient(double[][] data, double[] results) {
        double[] predictions = new double[data.length];
        double[] observations = new double[data.length];
        int validCount = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i][1] != Double.NaN) {
                observations[validCount] = data[i][1];
                predictions[validCount] = results[i];
                validCount++;
            }
        }

        double[] validObservations = Arrays.copyOf(observations, validCount);
        double[] validPredictions = Arrays.copyOf(predictions, validCount);

        double sum = 0;
        for (double obs : validObservations) {
            sum += obs;
        }
        double mean = sum / validObservations.length;

        double ssyy = 0;
        for (double obs : validObservations) {
            double difference = obs - mean;
            ssyy += difference * difference;
        }

        double sse = 0;
        for (int i = 0; i < validObservations.length; i++) {
            double residual = validObservations[i] - validPredictions[i];
            sse += residual * residual;
        }

        return 1 - (sse / ssyy);
    }

    /**
     * Determine the solution of a system of linear equations A * x = b using
     * Gaussian elimination.
     */
    public static double[] gaussianElimination(double[][] input, int order) {
        double[][] matrix = input;
        int n = input.length - 1;
        double[] coefficients = new double[order];

        for (int i = 0; i < n; i++) {
            int maxrow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(matrix[i][j]) > Math.abs(matrix[i][maxrow])) {
                    maxrow = j;
                }
            }

            for (int k = i; k < n + 1; k++) {
                double tmp = matrix[k][i];
                matrix[k][i] = matrix[k][maxrow];
                matrix[k][maxrow] = tmp;
            }

            for (int j = i + 1; j < n; j++) {
                for (int k = n; k >= i; k--) {
                    matrix[k][j] -= (matrix[k][i] * matrix[i][j]) / matrix[i][i];
                }
            }
        }

        for (int j = n - 1; j >= 0; j--) {
            double total = 0;
            for (int k = j + 1; k < n; k++) {
                total += matrix[k][j] * coefficients[k];
            }
            coefficients[j] = (matrix[n][j] - total) / matrix[j][j];
        }

        return coefficients;
    }

    /**
     * Round a number to a precision, specified in number of decimal places
     */
    public static double round(double number, int precision) {
        double factor = Math.pow(10, precision);
        return Math.round(number * factor) / factor;
    }
}