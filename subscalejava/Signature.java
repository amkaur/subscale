package subscalejava;

import java.io.Serializable;
import java.util.List;

public class Signature implements Serializable{
    private long sum;    
    private List <Integer> points;
    
    public Signature(long s,List <Integer> pt)
    {
        sum=s;
        points=pt;         
    }
  
    public long getSum() 
    {
        return sum;
    }         
    public void addSum(Long s) 
    {
        this.sum=this.sum+s;
    }    
    public void addPoints(List <Integer> pt) 
    {
        this.points.addAll(pt);
    }  
    public List<Integer> getPoints ()
    {
       return points; 
    } 
}
