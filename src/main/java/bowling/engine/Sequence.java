package bowling.engine;

public interface Sequence extends IntWrapper {
    int FIRST = 1;
    int LAST = 10;

    Sequence next();
    boolean isFinal();
}
