package ruse.classes.stats;

public class ExponentialRegression {
    public double coeffA;
    public double coeffB;
    public double[] points;
    public String string;
    public double r2;
    public RegressionOptions options;
    public double[] equation;

    public ExponentialRegression(double[][] data) {
        this.options = new RegressionOptions(2, 2);
        double[] sum = new double[6];

        for (int n = 0; n < data.length; n++) {
            if (!Double.isNaN(data[n][1])) {
                sum[0] += data[n][0];
                sum[1] += data[n][1];
                sum[2] += data[n][0] * data[n][0] * data[n][1];
                sum[3] += data[n][1] * Math.log(data[n][1]);
                sum[4] += data[n][0] * data[n][1] * Math.log(data[n][1]);
                sum[5] += data[n][0] * data[n][1];
            }
        }

        double denominator = (sum[1] * sum[2]) - (sum[5] * sum[5]);
        double a = Math.exp(((sum[2] * sum[3]) - (sum[5] * sum[4])) / denominator);
        double b = ((sum[1] * sum[4]) - (sum[5] * sum[3])) / denominator;

        this.coeffA = StatUtils.round(a, this.options.precision);
        this.coeffB = StatUtils.round(b, this.options.precision);
        this.points = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            this.points[i] = this.predict(data[i][0]);
        }
        this.equation = new double[]{this.coeffA, this.coeffB};
        this.string = "y = " + this.coeffA + "e^(" + this.coeffB + "x)";
        this.r2 = StatUtils.round(StatUtils.determinationCoefficient(data, this.points), this.options.precision);
    }

    public double predict(double x) {
        return StatUtils.round(this.coeffA * Math.exp(this.coeffB * x), this.options.precision);
    }
}