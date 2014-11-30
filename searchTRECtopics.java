package searchTRECtopics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;


import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class searchTRECtopics {
	public String pathToIndex = null;
	public String queryFilePath = null;
	public String homeDir = null;
	Map<String,Double> relScoreMap=new TreeMap<String, Double>();
	
	/*
	 * searchTRECtopics - Constructor to initialize class variables.
	 */
	
	searchTRECtopics(String pathName, String queryFilePath){
		this.pathToIndex = pathName;
		this.queryFilePath = queryFilePath;
		setHomeDir(queryFilePath);
		relScoreMap.clear();
	}
	
	/*
	 * setHomeDir- setter for homeDir 
	 */
	public void setHomeDir(String x) { 
		File queryFile = new File(x);  
		this.homeDir = queryFile.getAbsoluteFile().getParentFile().getAbsolutePath();
		System.out.println("Setting the directory location for storing the output files as: \"" +homeDir +"\"");
		}
	
	/* computeRelScore
	 * Calculates relevance score for every query and outputs the first 1000 relevant documents to a text file.
	 * Input parameters: List of queries, list of query numbers and flag.
	 */
	
	public void computeRelScore(ArrayList<String> queryArray, ArrayList<String> listOfQUERYs,int flag) throws ParseException, IOException{
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		String reportFile;
		String runValue; 
		File dir = new File(homeDir);
		
		if(flag ==1){ //check if short/long version of the query and set output file names accordingly
			reportFile = "SearchTRECshortQuery.txt";
			runValue = "run-TITLE";
		}
		else{
			reportFile = "SearchTREClongQuery.txt";
			runValue = "run-DESC";
		}
		File resultFile = new File (dir, reportFile);
		System.out.println("Output File Name:" +reportFile);
		resultFile.createNewFile();
		PrintWriter writer = new PrintWriter(resultFile, "ASCII");
		//writer.println("QueryID \t Q0 \t DocID \t \t Rank \t Score \t\t\t RunID");
		for(int i=0; i<queryArray.size();i++){
			relScoreMap.clear();
			Query query = parser.parse(parser.escape(queryArray.get(i)));
			Set<Term> queryTerms = new LinkedHashSet<Term>();
			Integer docCount;
			double relevanceFactor = 0;
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
			IndexSearcher searcher = new IndexSearcher(reader);
			int numOfDocsInCorpus = reader.maxDoc();
			query.extractTerms(queryTerms);
			for (Term t : queryTerms) { //for every query term in the given query
					docCount = reader.docFreq(t);
					if(docCount > 0)
					{
						
						double IDF = Math.log10(1+(numOfDocsInCorpus/docCount)); //calculate IDF for the query term
						DefaultSimilarity dSimi=new DefaultSimilarity();
						List<AtomicReaderContext> leafContexts = reader.getContext().reader().leaves();
						for (int i1 = 0; i1 < leafContexts.size(); i1++) { //for all leafs in the index
								AtomicReaderContext leafContext=leafContexts.get(i1);
								int startDocNo=leafContext.docBase;
								int numberOfDoc=leafContext.reader().maxDoc();
								DocsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),MultiFields.getLiveDocs(leafContext.reader()),"TEXT", new BytesRef(t.text())); //get the list of docs hacing the query term
								int doc=0;
								
								while (de!= null && (doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) { //until all the relevant docs for the query term are exhausted
										//Document doci = searcher.doc(doc);
										Document docu = searcher.doc(de.docID()+startDocNo);
										String DocID = docu.get("DOCNO");
										float normDocLeng=dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(doc));
										double normalizedTermFrequency = de.freq()/normDocLeng; //normalized term frequency computation
										relevanceFactor = normalizedTermFrequency * IDF; //relevance score for a particular doc
										addValues(DocID, relevanceFactor);
												
								}
										
						}
 
					}
					else{
						System.out.println("Search term which doesn't exist in the index vocabulary: " +t.text());
					}
				}
			
			List<Entry<String, Double>> sortedMap = entriesSortedByValues(relScoreMap); //sort the map on values
			for(int it = 0,count = 1;it < sortedMap.size() && count <= 1000; it++, count++) { // output first 1000 ranked documents  to the report file
				writer.println(Integer.valueOf(listOfQUERYs.get(i).toString().trim())+" \t\t 0 \t "+sortedMap.get(it).getKey()+" \t "+count+" \t "+sortedMap.get(it).getValue()+" \t "+runValue);
	        }
		    reader.close();
		}
		writer.close();			
}
	
	/*
	 * entriesSortedByValues - Sorts a tree map on values
	 * Input - Tree map containing doc and relevance scores
	 * return - Tree map sorted on Values.
	 */ 
	public	static <K,V extends Comparable<? super V>> 
	    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
	
	List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
	
	Collections.sort(sortedEntries, 
	    new Comparator<Entry<K,V>>() {
	        @Override
	        public int compare(Entry<K,V> e1, Entry<K,V> e2) {
	            return e2.getValue().compareTo(e1.getValue());
	        }
	    }
	);

		return sortedEntries;
}

	/*
	 * addValues - checks for duplicate keys and adds to the previous value in tree map.
	 * input - key, value
	 * 
	 */
	
	public void addValues(String key, Double value) {
		   Double tempValue = 0.0;
		   if (relScoreMap.containsKey(key)) { //check if key exists and add the relevance score to the previous value
			   tempValue = relScoreMap.get(key);
			   tempValue += value;
			   relScoreMap.put(key, tempValue);
			   tempValue = 0.0;
		   } else {
			   relScoreMap.put(key, value); //directly insert docid, relevance score pair to the map
			   }
		}
	/*
	 * readForAP89Corpus - parser the topics file and returns a list of values between given field names
	 * input - doc content, fieldname1, fieldname2
	 * return - list of strings
	 */
	public ArrayList<String> readForAP89Corpus(String content,String fieldName, String fieldName2){
		String entireTrecText = content; 
		 int startDOC =0;
		 int locationOfDOCBegin = 0;
		 int locationOfDOCEnd = 0;
		 String oneOrMoreValues = "";
		 ArrayList<String> listOfFieldValues = new ArrayList<String>();
		 int entireTrecTextSize =  entireTrecText.length();
		  while (true) { 
			  	locationOfDOCBegin = entireTrecText.indexOf("<top>",startDOC);
				locationOfDOCEnd = entireTrecText.indexOf("</top>",startDOC);
				if (locationOfDOCBegin != -1 || locationOfDOCEnd != -1) { //check if <top> and </top> exists in the given query file 
					String perDocText = entireTrecText.substring(locationOfDOCBegin, locationOfDOCEnd); //get the string between <top> and </top>
					oneOrMoreValues = StringUtils.substringBetween(perDocText, fieldName, fieldName2); // get the string between given field names
					listOfFieldValues.add(oneOrMoreValues);
					startDOC = locationOfDOCEnd+"top".length();
					}
				// pass every doc content in a trec file for further processing
				
				else{
					break;
				}
				if (entireTrecTextSize <= startDOC) 
				  {
					  break;
				  }
			  }
		  return listOfFieldValues;  
	}
	
	/*
	 * inititateSearch - gets query list and passes onto relevance score computation function
	 * input - none
	 */

	
	public void inititateSearch() throws IOException, ParseException{
		try{
			File qFP = new File(queryFilePath);
			String text = new String(Files.readAllBytes(Paths.get(qFP.getAbsolutePath())));
			ArrayList<String> listOfQUERYs = readForAP89Corpus(text, "Number:", "<dom>"); // get the list of query numbers
			ArrayList<String> listOfTITLEs = readForAP89Corpus(text, "Topic:", "<desc>"); //get the queries from title field
			ArrayList<String> listOfDESCs = readForAP89Corpus(text, "Description:", "<smry>"); //get the queries descriptions field
			System.out.println("Number of Queries:" +listOfQUERYs.size());
			System.out.println("Number of Titles:" +listOfTITLEs.size());
			System.out.println("Number of Descriptions:" +listOfDESCs.size());
			computeRelScore(listOfTITLEs, listOfQUERYs,1); //call relevance score computing methods
			computeRelScore(listOfDESCs, listOfQUERYs,2);
		}
		catch (IOException ie) {
            ie.printStackTrace(); // Print stack trace to the console on exceptions.
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		try{ 
				Scanner consoleInput = new Scanner(System.in); //create scanner objects to fetch user input
				System.out.println("Enter the path to index");
				
				String pathToIndex = consoleInput.nextLine();
				System.out.println("Enter the path to query topics file");
				
				String queryFilePath = consoleInput.nextLine();	
				System.out.println("Calling searcher");
				
				//initiate the relevance score computation
				searchTRECtopics baby = new searchTRECtopics(pathToIndex, queryFilePath);
				baby.inititateSearch();
		}
		catch(IOException ie) {
            ie.printStackTrace(); // Print stack trace to the console on exceptions.
		}
		
		finally{
			System.out.println("------------------------------------------------------------------");
			System.out.println("Exiting Relevance Score computation process. Please stand by!");
			System.out.println("------------------------------------------------------------------");
		}

	}

}
