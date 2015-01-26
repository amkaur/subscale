/*
 * This class defines an entry for the CollisionMap data structure used in the Main program. 
 */
package subscalejava;
import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

public class CollisionMapEntry {
    private ActualComparableBitSet dimset;
    private List <Integer> points;
    
    public CollisionMapEntry(int DIM,int dim,List <Integer> pt)
    {
        points=pt;
        dimset=new ActualComparableBitSet(DIM);
        dimset.set(dim);
    }
     
    public void addDim(int dimnext)
    {
        dimset.set(dimnext);      
    }
    
    /**
     * Get the size of maximal subspace
     * @return
     */
    public int getNumDim()
    {
        return dimset.cardinality();
    }
    /**
     * Get maximal subspace
     * @return
     */
    public ActualComparableBitSet getDimSet() 
    {
        return dimset;
    }
    /**
     * If the set of dense points are found in the same single dimensional space again. (Checks if the Entry has been made already or not )
     * @param dim
     * @return
     */
    public boolean exists(int dim)
    {
        return dimset.get(dim);            
    }
        
    public List <Integer> getPoints()
     {
        return points;         
     }    

    
}
