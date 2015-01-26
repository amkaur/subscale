package subscalejava;
/**
 * @author akaur
 */
public class PointIDValue {
    private int ID;
    private double value;
    
    public PointIDValue(int id, double val) {
        ID=id;
        value=val;
    }    
    public void setNumValue(int id,double val)
    {
        ID=id;
        value=val;
    }
    public double getValue() {
        return value;
    }
    
    public int getNum() {    
        return ID;
    }
}
