package csd.util.Abstracts;

/**
 * Created by Johnny on 2017/7/19.
 */
public abstract class Tables {

    public abstract void buildIndex();

    public abstract double calMem();

    public abstract int[] query(int qNode, int kConstraint, int lConstraint);

    public abstract int getKCoreMax();
}
