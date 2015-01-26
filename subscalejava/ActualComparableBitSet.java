package subscalejava;

/**
 *
 * @author akaur
 */
public class ActualComparableBitSet extends java.util.BitSet implements Comparable {
    
    
    @Override
    public int compareTo(Object other) {
        if (!(other instanceof ActualComparableBitSet)) throw new IllegalArgumentException();
        else {
            long[] otherArray = ((ActualComparableBitSet)other).toLongArray();
            long[] thisArray = this.toLongArray();
            for (int i = 0; i < otherArray.length; i ++) {
                if (i >= thisArray.length) return -1;
                if (otherArray[i] > thisArray[i]) return -1;
                if (otherArray[i] < thisArray[i]) return 1;
            }
        }
        return 0;
    }
    
    public ActualComparableBitSet() {}
    
    public ActualComparableBitSet(int n) {
        super(n);
    }
    
    
}
