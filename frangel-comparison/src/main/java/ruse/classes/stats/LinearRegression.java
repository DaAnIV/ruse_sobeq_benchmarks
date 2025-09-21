package ruse.classes.stats;

public class LinearRegression {
    public double gradient;
    public double intercept;
    public double[] points;
    public String string;
    public double r2;
    public RegressionOptions options;
    public double[] equation;

    public LinearRegression(double[][] data) {
        this.options = new RegressionOptions(2, 2);

        double[] sum = new double[5];
        int len = 0;

        for (int n = 0; n < data.length; n++) {
            if (!Double.isNaN(data[n][1])) {
                len++;
                sum[0] += data[n][0];
                sum[1] += data[n][1];
                sum[2] += data[n][0] * data[n][0];
                sum[3] += data[n][0] * data[n][1];
                sum[4] += data[n][1] * data[n][1];
            }
        }

        double run = (len * sum[2]) - (sum[0] * sum[0]);
        double rise = (len * sum[3]) - (sum[0] * sum[1]);
        double gradient = run == 0 ? 0 : StatUtils.round(rise / run, this.options.precision);
        double intercept = StatUtils.round((sum[1] / len) - ((gradient * sum[0]) / len), this.options.precision);

        this.gradient = gradient;
        this.intercept = intercept;
        this.points = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            this.points[i] = this.predict(data[i][0]);
        }
        this.equation = new double[]{gradient, intercept};
        this.r2 = StatUtils.round(StatUtils.determinationCoefficient(data, this.points), this.options.precision);
        this.string = intercept == 0 ? "y = " + gradient + "x" : "y = " + gradient + "x + " + intercept;
    }

    public double predict(double x) {
        return StatUtils.round((this.gradient * x) + this.intercept, this.options.precision);
    }
}