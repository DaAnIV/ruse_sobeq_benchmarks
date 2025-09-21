package ruse.classes.stats;

public class LogarithmicRegression {
    public double coeffA;
    public double coeffB;
    public double[] points;
    public String string;
    public double r2;
    public RegressionOptions options;
    public double[] equation;

    public LogarithmicRegression(double[][] data) {
        this.options = new RegressionOptions(2, 2);
        double[] sum = new double[4];
        int len = data.length;
        
        for (int n = 0; n < len; n++) {
            if (!Double.isNaN(data[n][1])) {
                sum[0] += Math.log(data[n][0]);
                sum[1] += data[n][1] * Math.log(data[n][0]);
                sum[2] += data[n][1];
                sum[3] += Math.log(data[n][0]) * Math.log(data[n][0]);
            }
        }

        double a = ((len * sum[1]) - (sum[2] * sum[0])) / ((len * sum[3]) - (sum[0] * sum[0]));
        this.coeffB = StatUtils.round(a, this.options.precision);
        this.coeffA = StatUtils.round((sum[2] - (this.coeffB * sum[0])) / len, this.options.precision);

        this.points = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            this.points[i] = this.predict(data[i][0]);
        }
        this.equation = new double[]{this.coeffA, this.coeffB};
        this.string = "y = " + this.coeffA + " + " + this.coeffB + " ln(x)";
        this.r2 = StatUtils.round(StatUtils.determinationCoefficient(data, this.points), this.options.precision);
    }

    public double predict(double x) {
        return StatUtils.round(this.coeffA + (this.coeffB * Math.log(x)), this.options.precision);
    }
}