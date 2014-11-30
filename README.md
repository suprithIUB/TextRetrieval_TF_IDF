TextRetrieval_TF_IDF
====================

A Java implementation of text search algorithm based on TF &amp; IDF.


Implement your first search algorithm

For each given query, the code 1. Parse the query using Standard Analyzer, 2. Calculate the relevance score for each  query term, and 3. Calculate the relevance score ( F, 𝑑𝑜𝑐 ).

The class is called easySearch, which contains a constructor easySearch() and member methods computeRelScore(), addValues() and main(). 

Short description:
easySearch () – Sets up variables pertaining and index location and query string.
computeRelScore () – Handles the calculation of relevance score for the given query string.
addValues () – Handles the maintenance of the map object containing the computed relevance score.
main() – sets the ball rolling.

The output will contain a list of sum of relevance scores for each query term for all the relevant documents in the corpus.

Usage: easySearch.java 
Output Sample: 
Enter the path to index dir
D:\IR_Materials\assignment2\index\default
Enter the query string
new governor
Computing the relevance scores based on the given input
Total Number of Documents in the corpus: 84474
Query term: new
Number of docs matching the query term: new is:38592
IDF: 0.47712125471966244 ….
***************************************************************
Relevance Scores for all the relevant docs in the corpus for the given query is listed below
{AP890101-0001=30.535760302058396 ……


Test your search function with TREC topics 

Software outputs up to top 1000 search results to a result file in a format that enables the trec_eval program to produce evaluation reports. 

The class is called searchTRECtopics, which contains a constructor searchTRECtopics () and member methods setHomeDir (),computeRelScore (),entriesSortedByValues (),addValues(),readForAP89Corpus(),inititateSearch() and main().

Short description:
searchTRECtopics () – Sets up variables pertaining and index location, query file path and home dir location.
setHomeDir () – setter for homeDir
readForAP89Corpus() – Parse function for retrieving field values from trec text files.
computeRelScore () – Calculates relevance score for every query and outputs the first 1000 relevant documents to a text file.
entriesSortedByValues() – Sorts a tree map on values.
inititateSearch() – gets query list and passes onto relevance score computation function.
addValues() - Handles the maintenance of the map object containing the computed relevance score.
main() – sets the ball rolling.

Usage: searchTRECtopics.java 
Sample Output: 

Enter the path to index
D:\IR_Materials\assignment2\index\default
Enter the path to query topics file
D:\IR_Materials\assignment2\topics.51-100
Calling searcher
Setting the directory location for storing the output files as: "D:\IR_Materials\assignment2"
Number of Queries:50
Number of Titles:50
Number of Descriptions:50
Output File Name:SearchTRECshortQuery.txt
Output File Name:SearchTREClongQuery.txt

The output files will be placed in the file directory where topics.51-100 is located.

Test Other Search Algorithms

The software outputs two files each (for both short and long queries) of below listed retrieval and ranking algorithms.

1. Vector Space Model (org.apache.lucene.search.similarities.DefaultSimilarity)
2. BM25 (org.apache.lucene.search.similarities.BM25Similarity)
3. Language Model with Dirichlet Smoothing(org.apache.lucene.search.similarities.LMDirichletSimilarity)
4. Language Model with Jelinek Mercer Smoothing (org.apache.lucene.search.similarities.LMJelinekMercerSimilarity, set λ to 0.7)

Software outputs up to top 1000 search results to a result file in a format that enables the trec_eval program to produce evaluation reports. 

The class is called compareAlgorithms, which contains a constructor compareAlgorithms () and member methods setHomeDir (),computeRelScore (),entriesSortedByValues (),addValues(),readForAP89Corpus(),inititateSearch() and main().

Short description:
compareAlgorithms () – Sets up variables pertaining and index location, query file path and home dir location.
setHomeDir () – setter for homeDir
readForAP89Corpus() – Parse function for retrieving field values from trec text files.
computeRelScore () – Calculates relevance score for every query and outputs the first 1000 relevant documents to a text file.
entriesSortedByValues() – Sorts a tree map on values.
inititateSearch() – gets query list and passes onto relevance score computation function.
addValues() - Handles the maintenance of the map object containing the computed relevance score.
main() – sets the ball rolling.

Usage: compareAlgorithms.java 
Sample Output: 
Please enter the path to index
D:\IR_Materials\assignment2\index\default
Please enter the path to query topics file
D:\IR_Materials\assignment2\topics.51-100
Calling searcher
Setting the directory location for storing the output files as: "D:\IR_Materials\assignment2"
Number of Queries:50
Number of Titles:50
Number of Descriptions:50…….
The output files will be placed in the file directory where topics.51-100 is located.

NOTE: Please include the jar files lucene-analyzers-common-4.10.1.jar, lucene-core-4.10.1.jar, lucene-queries-4.10.1.jar, commons-lang3-3.3.2.jar and lucene-queryparser-4.10.1 while running the programs.


