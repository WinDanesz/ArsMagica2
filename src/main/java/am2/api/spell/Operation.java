package am2.api.spell;

import java.util.function.BiFunction;

public class Operation {

    public static final BiFunction<Double, Double, Double> ADD = (u, v) -> {
        return u + v;
    };
    public static final BiFunction<Double, Double, Double> SUBTRACT = (u, v) -> {
        return u - v;
    };
    public static final BiFunction<Double, Double, Double> MULTIPLY = (u, v) -> {
        return u * v;
    };
    public static final BiFunction<Double, Double, Double> DIVIDE = (u, v) -> {
        return u / v;
    };
    public static final BiFunction<Double, Double, Double> POW = (u, v) -> {
        return Math.pow(u, v);
    };
    public static final BiFunction<Double, Double, Double> REPLACE = (u, v) -> {
        return v;
    };
}
