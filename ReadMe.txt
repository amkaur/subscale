					======
					README
					======
			Programs for the SUBSCALE Algorithm

==================================================


1. Using the code
------------------


		----------- Step 1 -----------

The folder 'subscalejava' contains relevant code to run first part of the SUBSCALE algorithm. 

'subscalejava.SubscaleSeq' takes three parameters: 
1. datafile (csv format)
2. epsilon (double)
3. tau (integer). 

The program outputs a file 'maximaldense.csv' which contains dense points in the relevant maximal subspaces.
The first line is the metadata about the result (name of Input data file, SIZE of dataset, Total Dimensionality of the dataset, epsilon, tau, Execution time). Rest of the file is of the format: '{subspace}-[Dense points]'.

		
Example: java subscalejava.Subscale datafile.csv 0.01 3

		
		----------- Step 2 -----------

The python script 'refine.py' outputs the final maximal subspace clusters from maximal dense points in 'maximaldense.csv'. 

It takes two parameters:
1. epsilon (double) - The script can be changed to adapt epsilon values according to the dimensionality of the subspaces. 
2. minSize (integer) which is the minimum allowed size for a cluster. 

The final result is in the file 'result.csv'. The first line is the metadata about the results (similar to that in Step 1 with the additional execution time used by this script).

Example: python refine.py 0.01 4



2. Data
--------

The program uses normalised data between 0 and 1 in csv file format.

