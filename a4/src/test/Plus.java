package test;

public class Plus {
    public static void main(String[] args) {
        int a = 25;
        int b = 25 + 3 * a;
        int p = plus(a, b);
        System.out.println(p); // 125
    }

    private static int plus(int a, int b) {
        return a + b;
    }
}
