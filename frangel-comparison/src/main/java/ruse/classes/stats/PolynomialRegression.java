package ruse.classes.stats;

public class PolynomialRegression {
    public double[] coefficients;
    public double[] points;
    public String string;
    public double r2;
    public RegressionOptions options;
    public double[] equation;

    public PolynomialRegression(double[][] data) {
        this.options = new RegressionOptions(2, 2);
        double[][] lhs = new double[this.options.order + 1][];
        double[] rhs = new double[this.options.order + 1];
        double a = 0;
        double b = 0;
        int len = data.length;
        int k = this.options.order + 1;

        for (int i = 0; i < k; i++) {
            for (int l = 0; l < len; l++) {
                if (!Double.isNaN(data[l][1])) {
                    a += Math.pow(data[l][0], i) * data[l][1];
                }
            }

            rhs[i] = a;
            a = 0;

            double[] c = new double[k];
            for (int j = 0; j < k; j++) {
                for (int l = 0; l < len; l++) {
                    if (!Double.isNaN(data[l][1])) {
                        b += Math.pow(data[l][0], i + j);
                    }
                }
                c[j] = b;
                b = 0;
            }
            lhs[i] = c;
        }

        // Create augmented matrix for Gaussian elimination
        double[][] augmented = new double[k + 1][k];
        for (int i = 0; i < k; i++) {
            System.arraycopy(lhs[i], 0, augmented[i], 0, k);
        }
        System.arraycopy(rhs, 0, augmented[k], 0, k);

        double[] coefficients = StatUtils.gaussianElimination(augmented, k);
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = StatUtils.round(coefficients[i], this.options.precision);
        }

        this.coefficients = coefficients;
        this.points = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            this.points[i] = this.predict(data[i][0]);
        }

        StringBuilder sb = new StringBuilder("y = ");
        for (int i = coefficients.length - 1; i >= 0; i--) {
            if (i > 1) {
                sb.append(coefficients[i]).append("x^").append(i).append(" + ");
            } else if (i == 1) {
                sb.append(coefficients[i]).append("x + ");
            } else {
                sb.append(coefficients[i]);
            }
        }

        this.string = sb.toString();
        this.equation = coefficients.clone();
        // Reverse for equation array
        for (int i = 0; i < this.equation.length / 2; i++) {
            double temp = this.equation[i];
            this.equation[i] = this.equation[this.equation.length - 1 - i];
            this.equation[this.equation.length - 1 - i] = temp;
        }
        this.r2 = StatUtils.round(StatUtils.determinationCoefficient(data, this.points), this.options.precision);
    }

    public double predict(double x) {
        double sum = 0;
        for (int i = 0; i < this.coefficients.length; i++) {
            sum += this.coefficients[i] * Math.pow(x, i);
        }
        return StatUtils.round(sum, this.options.precision);
    }
}