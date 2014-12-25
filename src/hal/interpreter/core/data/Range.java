package hal.interpreter.core.data;

public class Range {
    public int start;
    public int end;
    public int step;
    public boolean include;

    public Range() {
        this.start = 0;
        this.end = 0;
        this.step = 1;
        this.include = false;
    }
};
