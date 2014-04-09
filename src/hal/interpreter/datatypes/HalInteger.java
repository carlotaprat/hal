package hal.interpreter.datatypes;

import hal.interpreter.DataType;

public class HalInteger extends DataType<Integer>
{
    public HalInteger(Integer i)
    {
        super(i);
    }

    public HalInteger __pos__()
    {
        return new HalInteger(value);
    }

    public HalInteger __neg__()
    {
        return new HalInteger(-value);
    }

    public HalBoolean __not__()
    {
        return new HalBoolean(toBoolean());
    }

    public HalInteger __add__(DataType d)
    {
        HalInteger i = new HalInteger(value);
        i.value += d.toInteger();
        return i;
    }

    public HalInteger __sub__(DataType d)
    {
        HalInteger i = new HalInteger(value);
        i.value -= d.toInteger();
        return i;
    }

    public HalInteger __mul__(DataType d)
    {
        HalInteger i = new HalInteger(value);
        i.value *= d.toInteger();
        return i;
    }

    public HalInteger __div__(DataType d)
    {
        int v = checkDivZero(d);
        HalInteger i = new HalInteger(value);
        i.value /= v;
        return i;
    }

    public HalInteger __mod__(DataType d)
    {
        int v = checkDivZero(d);
        HalInteger i= new HalInteger(value);
        i.value %= v;
        return i;
    }

    public HalString __str__() {
        return new HalString(Integer.toString(value));
    }

    public HalInteger __int__() {
        return new HalInteger(value);
    }

    public HalBoolean __bool__() {
        return new HalBoolean(value != 0);
    }

    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private int checkDivZero(DataType d)
    {
        int i = d.toInteger();

        if (i == 0)
            throw new RuntimeException ("Division by zero");

        return i;
    }
}
