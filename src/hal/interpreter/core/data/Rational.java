package hal.interpreter.core.data;

public class Rational extends Number {
    private int num;
    private int den;
    
    public Rational(Integer i) {
        num = i;
        den = 1;
    }
    
    public Rational(int n, int d) {
        int g = gcd(n,d);
        num = n/g;
        den = d/g;
        if (den < 0) {
            den = -den;
            num = -num;
        }
    }
    
    public Rational(Rational r) {
        num = r.num;
        den = r.den;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rational rational = (Rational) o;
        return den == rational.den && num == rational.num;
    }

    @Override
    public int hashCode() {
        int result = num;
        result = 31 * result + den;
        return result;
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int tmp = a;
            a = b;

            b = tmp % a;
        }
        return a;
    }
    
    public boolean isInt() {
        return den == 1;
    }
    
    public int getNum() {
        return num;
    }
    
    public int getDen() {
        return den;
    }
    
    public Rational add(Integer i) {
        return add(new Rational(i));
    }
    
    public Rational add(Rational r) {
        return new Rational(num*r.den + r.num*den, den*r.den);
    }
    
    public Rational sub(Integer i) {
        return sub(new Rational(i));
    }
    
    public Rational sub(Rational r) {
        return new Rational(num*r.den - r.num*den, den*r.den);
    }
    
    public Rational mul(Integer i) {
        return mul(new Rational(i));
    }
    
    public Rational mul(Rational r) {
        return new Rational(num*r.num, den*r.den);
    }
    
    public Rational div(Integer i) {
        return div(new Rational(i));
    }
    
    public Rational div(Rational r) {
        return new Rational(num*r.den, den*r.num);
    }
    
    @Override
    public int intValue() {
        return num/den;
    }
    
    @Override
    public long longValue() {
        return (long)num/den;
    }
    
    @Override
    public float floatValue() {
        return ((float)num)/den;
    }
    
    @Override
    public double doubleValue() {
        return ((double)num)/den;
    }
    
    @Override
    public String toString() {
        return Integer.toString(num)+"/"+Integer.toString(den);
    }
}
