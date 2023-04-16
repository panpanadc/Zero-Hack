package cum.xiaomao.zerohack.util.graphics;

@FunctionalInterface
public interface InterpolateFunction {
    float invoke(long time, float prev, float current);
}
