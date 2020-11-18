package opt.easyjmetal.util.distance;

/**
 * 距离计算
 */
@FunctionalInterface
public interface Distance<E, J> {
    double getDistance(E element1, J element2);
}
