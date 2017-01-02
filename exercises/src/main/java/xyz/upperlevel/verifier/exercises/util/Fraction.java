package xyz.upperlevel.verifier.exercises.util;

public class Fraction {
    public static final Fraction ZERO = new Fraction(0);
    public static final Fraction ONE = new Fraction(1);

    public final int num, den;

    public Fraction(int num, int den) {
        //int gcm = gcm(num, den);
        this.num = num;
        this.den = den;
    }

    public Fraction(int num) {
        this.num = num;
        this.den = 1;
    }

    public static int gcm(int a, int b) {
        return b == 0 ? a : gcm(b, a % b);
    }

    public int hashCode() {
        return num | (den >> 16);
    }

    public boolean equals(Object obj) {
        if(obj instanceof Fraction) {
            Fraction oth = (Fraction) obj;
            return (oth.num == num && oth.den == den);
        } else return false;
    }
}
