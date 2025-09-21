package ruse.classes.stats;

public class PowerRegression {
    public double coeffA;
    public double coeffB;
    public double[] points;
    public String string;
    public double r2;
    public RegressionOptions options;
    public double[] equation;

    public PowerRegression(double[][] data) {
        this.options = new RegressionOptions(2, 2);
        double[] sum = new double[5];
        int len = data.length;

        for (int n = 0; n < len; n++) {
            if (!Double.isNaN(data[n][1])) {
                sum[0] += Math.log(data[n][0]);
                sum[1] += Math.log(data[n][1]) * Math.log(data[n][0]);
                sum[2] += Math.log(data[n][1]);
                sum[3] += Math.log(data[n][0]) * Math.log(data[n][0]);
            }
        }

        double b = ((len * sum[1]) - (sum[0] * sum[2])) / ((len * sum[3]) - (sum[0] * sum[0]));
        double a = (sum[2] - (b * sum[0])) / len;
        this.coeffA = StatUtils.round(Math.exp(a), this.options.precision);
        this.coeffB = StatUtils.round(b, this.options.precision);

        this.points = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            this.points[i] = this.predict(data[i][0]);
        }
        this.equation = new double[]{this.coeffA, this.coeffB};
        this.string = "y = " + this.coeffA + "x^" + this.coeffB;
        this.r2 = StatUtils.round(StatUtils.determinationCoefficient(data, this.points), this.options.precision);
    }

    public double predict(double x) {
        return StatUtils.round(this.coeffA * Math.pow(x, this.coeffB), this.options.precision);
    }
}