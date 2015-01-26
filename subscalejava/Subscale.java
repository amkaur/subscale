/*
 *Program to find the maximal subspace dense points (without splitting the hashtable)
 */

package subscalejava;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
  * @author akaur
 */

public class Subscale {

    public static int SIZE,DIM,TAU; //SIZE is total number of points, DIM is dimensionality of dataset        
    public static String dataFile;     
    public static long runTime; 
    public static double EPSILON=0.001;
    public static Map <Integer,Long> inttolong=new HashMap<>(); // Maps point ID to large keys   
    public static Map <BitSet,Set<Integer>> SSMap=new HashMap<>(); //Contains dense points in maximal subspaces
   
//Main Program
    /** @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {  
                    /****************** SET PARAMETERS **********************/
        dataFile=args[0];
        EPSILON=Double.parseDouble(args[1]);
        TAU=Integer.parseInt(args[2]);
                    /*******************************************************/
        
        List<List<Double>> dataMatrix;    // Input data matrix of points (SIZE X DIM)
        
        //Reading input file into dataMatrix
        int pointID;    
        try (Scanner input = new Scanner(new File(dataFile))) {   
            dataMatrix=new ArrayList<List<Double>>();
            Random keyGenerator = new Random();           
            pointID=0;   
            input.nextLine();            
            while(input.hasNextLine()) {
                List <Double> pointVector=new ArrayList<Double>();
                long rand=(long)(1000000000000L+keyGenerator.nextDouble()*1000000000000L);                
                inttolong.put(pointID,rand);                                
                input.useDelimiter(",") ;                                                
                while(input.hasNextDouble()) {
                    pointVector.add(input.nextDouble());   
                }
                pointVector.add(Double.parseDouble(input.nextLine().replace(",","")));
                dataMatrix.add(pointVector);  
                pointID++;
            } 
            SIZE=dataMatrix.size();
            DIM=dataMatrix.get(0).size();
            input.close();
        }   
        
        PointIDValue[] dimVector=new PointIDValue [SIZE];   //1-dimensional points projected on dimension 'dim', where 0<=dim<DIM
        List<List<List<Integer>>> epChunk=new ArrayList<List<List<Integer>>>();    //Data structure to store epsilon chunks (core-sets) created in all single dimensions
        
        long startTime=System.currentTimeMillis();  //Start calculating the time once all initial data is in working memory
        
        //Sort point values in each dimension and then create epsilon chunks.
        for(int dim=0;dim<DIM;dim++) {
            for(pointID=0;pointID<SIZE;pointID++) {
               dimVector[pointID]=new PointIDValue(pointID,dataMatrix.get(pointID).get(dim));              
            }             
            sort(dimVector);    
            epChunk.add(createDensityChunks(dimVector));            
        }  
        
        collisions(epChunk);          
        long endTime=System.currentTimeMillis();
        runTime=endTime-startTime;           
        writeDensePoints();       
    }   
 
    /**
     * @param dimVector : dataMatrix projected on a single dimension
     * 
     */
    private static List<List<Integer>> createDensityChunks(PointIDValue [] dimVector) {
        
       List <List<Integer>> chunkList=new ArrayList<List<Integer>>(); //Contains all possible epsilon density chunks in a given dimVector 
       int newLast,last=-1; //newLast: Last element of the current epsilon chunk, last: Last element of the previous epsilon chunk      
    
       for(int ptr=0;ptr<SIZE;ptr++) {  //ptr is a pointer to current element in the dimVector
           List <Integer> chunk=new ArrayList<Integer>(); 
           int count=0;     //count the number of neighbours within epsilon distance       
           int next=ptr+1;
           chunk.add(dimVector [ptr].getNum()); // Add point at current location to the chunk
           
           while ((next<SIZE)&& ( dimVector[next].getValue()-dimVector[ptr].getValue())<EPSILON) {
               chunk.add(dimVector [next].getNum());  //Add the neighbours within epsilon distance
               next++;
               count++;              
            }          
            newLast=chunk.get(count);            
            if(newLast!=last) { /*E.g. if last chunk was {2,3,4,5,6} and new chunk is {3,4,5,6}, 
                                * new chunk will not generate any additional combination of points than the previous one */               
                last=newLast;
                if(count>=TAU) {    //atleast TAU neighbours
                    chunkList.add(chunk);
                }
            }            
        }         
        return chunkList;
    }   
  
    /**
     * Find dense units in the maximal subspaces
     * @param epChunk
     */
    public static void collisions(List<List<List<Integer>>> epChunk) {
       Map <Long,CollisionMapEntry> collisionMap;
       //For each point as a FOCUS, find its (TAU+1) combinations with the rest of points in the chunk - in every single dimension
       for(int focusPointID=0;focusPointID<SIZE-TAU;focusPointID++) {  
           //SIZE-TAU as the last TAU points would already have been assembled in some chunk
           
           collisionMap=new HashMap<>();            
           for(int dim=0;dim<DIM;dim++) {             
               int newLast,last=-1;
               boolean foundBefore=false;
               int chunkCount=0;
               
               while(chunkCount <epChunk.get(dim).size()) {
                   List <Integer> chunk=epChunk.get(dim).get(chunkCount);                   
                   int index=chunk.indexOf(focusPointID);  //Get the location of focus point in the current density chunk
                  
                   if(index==(-1)) {  //If the point does not exists in the current chunk, then decide if to look ahead in other chunks or abort
                          if(foundBefore) { /*If the focus point existed in the previous chunks but does not exists in the current chunk it means it will not exist in rest of the chunks, 
                                            as the points are sorted already according to their values in each dimension. So, abort the search. */
                                break;
                          }
                          else {
                              chunkCount++;  //The focus point was never found in previous chunks, so look ahead in rest of the chunks
                              continue;
                          }
                    } 
                   
                    int pivot=chunk.size()-1; //Pivot is used for optimization to avoid computing the combinations which had already been computed in previous chunks
                    
                    if(foundBefore) {      
                        newLast=chunk.get(pivot); 
                        while(pivot>TAU) {  //find the position of last element in this new chunk                                                   
                                pivot--;
                                if(chunk.get(pivot)== last)
                                    break;
                        }
                        if(pivot<TAU+1) {//implies that all of the combinitions in the given chunk need to be generated again
                                pivot=chunk.size()-1;
                        }
                        last=newLast; 
                     }
                    else {                   
                        last=chunk.get(pivot);
                        foundBefore=true;
                    }    
                    
                    List <Signature> listOfSignatures=getSignatures(chunk,index,pivot);  
                    long sum;
                    for (Signature listOfSignature : listOfSignatures) {
                       sum = listOfSignature.getSum();
                       
                       if (collisionMap.containsKey(sum)) { //if already exists
                           if(!(collisionMap.get(sum).exists(dim))) //Check if the collision if from two sums from different dimensions
                               collisionMap.get(sum).addDim(dim);
                       } else {
                           collisionMap.put(sum, new CollisionMapEntry(DIM, dim, listOfSignature.getPoints()));
                       }
                   }
                    if(chunk.size()<(TAU+1)) {
                       epChunk.get(dim).remove(chunkCount);                      
                   }
                    else {
                        chunkCount++; 
                    }
               }
           }           
           
           for (Iterator<Entry<Long, CollisionMapEntry>> it = collisionMap.entrySet().iterator(); it.hasNext();) {
               Map.Entry<Long,CollisionMapEntry> entry = it.next();
               if(entry.getValue().getNumDim()==1) {
                  it.remove();  // remove all those signatures which didn't collide
               }
           }         
           if(collisionMap.size()>0) {    
               refineSubspaces(collisionMap);
           }
       }
    }
    
      /**
     * Sift through the collisionMap to collect the dense points in each maximal subspace     * 
     */
    
    private static void refineSubspaces(Map <Long,CollisionMapEntry> collisionMap)
    {     
        for(Map.Entry<Long,CollisionMapEntry> entry : collisionMap.entrySet())
        {                     
            CollisionMapEntry me=entry.getValue();
            BitSet dimset=me.getDimSet();
            List <Integer> newPoints=me.getPoints();           
            if(SSMap.containsKey(dimset)) {
                SSMap.get(dimset).addAll(newPoints);
            }
            else {
                Set <Integer> points=new HashSet<>(newPoints);               
                SSMap.put(dimset,points);              
            }               
        }        
    }   
    
    /**
     * Given a density chunk of points this function generates the combinations out of it. Size of each combination is TAU+1.
     * The chunk is divided into leftChunk and rightChunk using pivot. And there can be three cases.    
     * 1. Right chunk is null -> The points are all new. We need to generate combinations for whole rightChunk
     * 2. Size of right chunk is more than TAU -> Independent dense units need to be extracted from it as well as those with the coordination from left.
     *    e.g. Lc= (1,2,3,4,5), Rc=(6,7,8,9,10,11,12). For TAU=3, we should get <1,2,3,6> as well as <6,7,8,9>.
     * 3. Size of right chunk is less than TAU -> there are no independent units and all units should be extracted with the coordination of left chunk.    
     */
    private static List <Signature> getSignatures(List<Integer> chunk,int ptindex,int pivot)
    {       
        List <Signature> listOfSignatures=new ArrayList<Signature>();
        int stablept=chunk.remove(ptindex);       
        List<Integer> leftChunk=chunk.subList(0, pivot);      
        List<Integer> rightChunk=chunk.subList(pivot,chunk.size());       
        int combiSize,rightCombi;
        
        if(rightChunk.size()>TAU) {
            combiSize=TAU;           
            rightCombi=1;
        }
        else {
            combiSize=rightChunk.size();
            if(rightChunk.size()>0)
                rightCombi=1;
            else
                rightCombi=0;            
        }   
        
        do {           
            listOfSignatures.addAll(sum(stablept,leftChunk,findCombinations(leftChunk.size(),TAU-rightCombi),rightChunk,findCombinations(rightChunk.size(),rightCombi)));
            rightCombi++;  
        }
        while(rightCombi<=combiSize);     
        return listOfSignatures;            
    }
     
     /**
     * Find all combinations of size 'combiSize' from a set of size 'chunkSize'.
     * @param chunkSize
     * @param combiSize
     * @return
     */
    private static List <List<Integer>> findCombinations(int chunkSize,int combiSize) {
        List <List<Integer>> combiData=new ArrayList<List<Integer>>();
        
        if( (combiSize==0) ||  (chunkSize==0) ){
            return null;
        }
        
        int pos;        
        List <Integer> combi=new ArrayList<Integer>(combiSize);
        for(int i=0;i<combiSize;i++) {
         combi.add(i,i);           
        }
        combiData.add(new ArrayList<Integer>(combi));         
        pos=combiSize-1;
        
        while(true) {
            if(combi.get(pos)<(chunkSize-combiSize+pos)) {
                combi.set(pos,combi.get(pos)+1);              
                if(pos==(combiSize-1)) {
                    combiData.add(new ArrayList<Integer>(combi));                   
                }                  
                else {
                    combi.set(pos+1,combi.get(pos));
                    pos++;
                }   
            }
            else {
                pos--;
            }
            if(pos==-1) {
                break;
            }
        } 
      return combiData;            
    }
     
    /*
     * Takes a list of density connected points and returns a list of signature sums    
     */
    private static List <Signature> sum(int stableptInd,List<Integer> leftChunk,List <List<Integer>> leftCombi,List<Integer> rightChunk,List <List<Integer>> rightCombi)
    {        
        List <Signature> listOfSignatures=new ArrayList<Signature>();
        long stablept=inttolong.get(stableptInd);        
        if(leftCombi==null){
            for (List<Integer> rightCombi1 : rightCombi) {
                long sum2=stablept;
                List<Integer> ptarray2=new ArrayList<Integer>();
                ptarray2.add(stableptInd);
                Iterator <Integer> it2 = rightCombi1.iterator();
                while(it2.hasNext()) {
                    int temp=rightChunk.get(it2.next());           
                    ptarray2.add(temp);
                    sum2+=inttolong.get(temp); 
                }
                listOfSignatures.add(new Signature(sum2,ptarray2)); 
            }            
        }
        else {
            for (List<Integer> leftCombi1 : leftCombi) {
                Iterator <Integer> it1 = leftCombi1.iterator();
                long sum1=stablept;
                List<Integer> ptarray1=new ArrayList<Integer>();
                ptarray1.add(stableptInd);
                while(it1.hasNext()) {
                    int temp=leftChunk.get(it1.next());           
                    ptarray1.add(temp);
                    sum1+=inttolong.get(temp);
                }             
                if(rightCombi==null) {
                    listOfSignatures.add(new Signature(sum1,ptarray1));   
                }
                else {
                    for (List<Integer> rightCombi1 : rightCombi) {
                        long sum2=sum1;
                        List<Integer> ptarray2=new ArrayList<Integer>(ptarray1);
                        Iterator <Integer> it2 = rightCombi1.iterator();
                        while(it2.hasNext()) {
                            int temp=rightChunk.get(it2.next());           
                            ptarray2.add(temp);
                            sum2+=inttolong.get(temp); 
                        }
                        listOfSignatures.add(new Signature(sum2,ptarray2));
                    }
                }
            }
        }
        return listOfSignatures;
    }    
      
   
    /**
     * Write the maximal subspace dense points to the output file
     * @throws IOException
     */
    private static void writeDensePoints() throws IOException  {         
               
         try(PrintWriter out = new PrintWriter(new FileWriter("maximaldense.csv")))  {              
            out.print(dataFile);
            out.print("-");
            out.print(SIZE);
            out.print("-");
            out.print(DIM);
            out.print("-");
            out.print(EPSILON);
            out.print("-");
            out.print(TAU);      
            out.print("-");
            out.print(runTime);                       
            out.println();
            for(Map.Entry<BitSet,Set<Integer>> entry : SSMap.entrySet()) {
                out.print(entry.getKey()); 
                out.print("-");
                out.print(entry.getValue());
                out.println();
            }
            out.flush();
         }
         catch(IOException ioe) {
        }
   }
   
    /**
     * Take a two-column array as input. ANd sort on the second column i.e. values
     * @param dimVector
     * @throws IOException
     */       
    private static void sort(PointIDValue [] dimVector) {
        sort(dimVector,0,dimVector.length-1);
    }
          
    private static void sort(PointIDValue [] dimVector, int first, int last) {
        if (last > first) {
            int pivotIndex = partition(dimVector, first, last);
            sort(dimVector, first, pivotIndex - 1);
            sort(dimVector, pivotIndex + 1, last);
        }
    }

  /** Partition the array list[first..last] */
  private static int partition(PointIDValue [] dimVector, int first, int last) {
    double pivot = dimVector[first].getValue(); // Choose the first element as the pivot
    int p=dimVector[first].getNum();
    int low = first + 1; // Index for forward search
    int high = last; // Index for backward search

    while (high > low) {
      // Search forward from left
      while (low <= high && dimVector[low].getValue() <= pivot) {
            low++;
        }

      // Search backward from right
      while (low <= high && dimVector[high].getValue() > pivot) {
            high--;
        }

      // Swap two elements in the list
      if (high > low) {
        double temp = dimVector[high].getValue();
        int t=dimVector[high].getNum();
        dimVector[high].setNumValue(dimVector[low].getNum(),dimVector[low].getValue());
        dimVector[low].setNumValue(t,temp);        
      }
    }

    while (high > first && dimVector[high].getValue() >= pivot) {
          high--;
      }

    // Swap pivot with list[high]
    if (pivot > dimVector[high].getValue()) {
      dimVector[first].setNumValue(dimVector[high].getNum(),dimVector[high].getValue());
      dimVector[high].setNumValue(p,pivot);
      return high;
    }
    else {
      return first;
    }
  }

}
