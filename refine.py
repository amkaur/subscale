# -*- coding: utf-8 -*-
"""
@author: akaur
"""

import csv
import sys
import numpy as np
import datetime
from sklearn.cluster import DBSCAN
from decimal import Decimal

start_time = datetime.datetime.now()

dbscan_epsi=Decimal(sys.argv[1])
minSize=int(sys.argv[2])


with open("maximaldense.csv") as csvINfile:
    inFile=csv.reader(csvINfile,quoting=csv.QUOTE_NONE,delimiter='-')
    metaRow=inFile.next()
    dataFile=metaRow[0]
    SIZE=metaRow[1]
    DIM=metaRow[2]
    EPSILON=metaRow[3]      
    TAU=metaRow[4]          
    subscaleRunTime=metaRow[5]
  
    #Prepare input data matrix for dbscan (for each maximal subspace found by SubscaleSeq.java)
    with open(dataFile,"rU") as csvDATAfile:  
        datacsv=csv.reader(csvDATAfile,quoting=csv.QUOTE_NONE,delimiter=',')           
        datacsv=list(datacsv)
        del datacsv[0]
        Allclusters=[]        
        for row in inFile: #for each found subspace in maximaloutput.csv
            DBcluster=[]   
            SSdata=[]                
            dim=map(int,row[0][1:-1].split(","))                
            DBcluster.append(dim)       
            points=map(int,row[1][1:-1].split(","))       
            for count,p in enumerate(points):
                SSdata.append([])                
                for d in dim:
                    SSdata[count].append(datacsv[p][d])                     
            X = np.array(SSdata) 
            
            #Run Scikit-learn DBSCAN on each subspace
            db=DBSCAN(eps=dbscan_epsi, min_samples=minSize).fit(X)      
            labels = db.labels_               
            foundCluster=False
            for k in set(labels):                
                if k == -1:                    
                    continue
                else:
                    clusters = [points[index[0]] for index in np.argwhere(labels == k)]  
                    DBcluster.append(clusters) 
                    foundCluster=True
            if(foundCluster):
                Allclusters.append(DBcluster)                
                
    end_time = datetime.datetime.now()                   
    DBruntime_temp=end_time-start_time 
    DBruntime=(DBruntime_temp.seconds*1000)+(DBruntime_temp.microseconds/1000)
    totaltime=int(subscaleRunTime)+DBruntime
    
    parameters=[]  
    parameters.append(dataFile)
    parameters.append(SIZE)
    parameters.append(DIM)
    parameters.append(EPSILON)
    parameters.append(TAU)   
    parameters.append(subscaleRunTime)       
    parameters.append(DBruntime)    
    parameters.append(totaltime)   
    outFile='result.csv'
    with open(outFile, 'w') as csvOUTfile:
        output=csv.writer(csvOUTfile)
        output.writerow(parameters)
        output.writerows(Allclusters)             