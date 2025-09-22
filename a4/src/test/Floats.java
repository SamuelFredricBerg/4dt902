package test;

public class Floats {
    public static void main(String[] args) {
        double f = 2.34;
        double ff = 2.0;
        double fff = mult(f, ff);
        System.out.println(fff); // 4.68

    }

    private static double mult(double a, double b) {
        return a * b;
    }
}
