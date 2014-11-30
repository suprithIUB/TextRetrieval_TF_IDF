package compareAlgorithms;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class compareAlgorithms {


		public String pathToIndex = null;
		public String queryFilePath = null;
		public String homeDir = null; 
		Map<String, Float> relScoreMap=new TreeMap<String, Float>();
		
		/*
		 * compareAlgorithms - Constructor to initialize class variables.
		 */
		
		compareAlgorithms(String pathName, String queryFilePath){
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
		
		/**
		 * @param args
		 */		

		public static void main(String[] args) throws ParseException, IOException {
			// TODO Auto-generated method stub
			try{ 
					Scanner consoleInput = new Scanner(System.in); // create scanner object to fetch user input
					System.out.println("Please enter the path to index");
					
					String pathToIndex = consoleInput.nextLine();
					System.out.println("Please enter the path to query topics file");
					
					String queryFilePath = consoleInput.nextLine();	
					System.out.println("Calling searcher");
					// initiate the core
					compareAlgorithms baby = new compareAlgorithms(pathToIndex, queryFilePath);
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
	
		/*
		 * inititateSearch - gets query list and passes onto relevance score computation function
		 * input - none
		 */
		
		public void inititateSearch() throws IOException, ParseException{
			try{
				File qFP = new File(queryFilePath);
				//create different similarity type objects that needs to executed
				Similarity type1 = new DefaultSimilarity();
				Similarity type2 = new BM25Similarity();
				Similarity type3 = new LMDirichletSimilarity();
				Similarity type4 = new LMJelinekMercerSimilarity((float) 0.7);
				String text = new String(Files.readAllBytes(Paths.get(qFP.getAbsolutePath()))); //get the full content of the query file
				ArrayList<String> listOfQUERYs = readForAP89Corpus(text, "Number:", "<dom>"); //get the doc numbers
				ArrayList<String> listOfTITLEs = readForAP89Corpus(text, "Topic:", "<desc>"); // get the queries from title field
				ArrayList<String> listOfDESCs = readForAP89Corpus(text, "Description:", "<smry>"); //get the queries from desc field
				System.out.println("Number of Queries:" +listOfQUERYs.size());
				System.out.println("Number of Titles:" +listOfTITLEs.size());
				System.out.println("Number of Descriptions:" +listOfDESCs.size());
				System.out.println("Intiating Vector Space Ranking Algorithm"); // 
				computeRelScore(listOfTITLEs, listOfQUERYs, type1 ,1);
				computeRelScore(listOfDESCs, listOfQUERYs, type1, 2);
				System.out.println("Vector Space Ranking Completed");
				System.out.println("Intiating BM25 Ranking Algorithm");
				computeRelScore(listOfTITLEs, listOfQUERYs, type2 ,1);
				computeRelScore(listOfDESCs, listOfQUERYs, type2, 2);
				System.out.println("BM25 Ranking Completed");
				System.out.println("Intiating LM Dirichlet Ranking Algorithm");
				computeRelScore(listOfTITLEs, listOfQUERYs, type3 ,1);
				computeRelScore(listOfDESCs, listOfQUERYs, type3, 2);
				System.out.println("LM Dirichlet Ranking Completed");
				System.out.println("Intiating LM Jelinek Mercer Ranking Algorithm");
				computeRelScore(listOfTITLEs, listOfQUERYs, type4 ,1);
				computeRelScore(listOfDESCs, listOfQUERYs, type4, 2);
				System.out.println("LM Jelinek Mercer Ranking Completed");
				System.out.println("Please find two result files for each ranking algorithms in the query file folder");
				
			}
			catch (IOException ie) {
	            ie.printStackTrace(); // Print stack trace to the console on exceptions.
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
						if (locationOfDOCBegin != -1 || locationOfDOCEnd != -1) { //check if both the tags top and </top> exist
							String perDocText = entireTrecText.substring(locationOfDOCBegin, locationOfDOCEnd); //get the content between <top> to </top>
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
	
	
		/* computeRelScore
		 * Calculates relevance score for every query and outputs the first 1000 relevant documents to a text file.
		 * Input parameters: List of queries, list of query numbers, ranking algorithm type and flag.
		 */
		
		public void computeRelScore(ArrayList<String> queryArray, ArrayList<String> listOfQUERYs, Similarity similarityType, int flag) throws ParseException, IOException{
			String reportFile;
			String runvalue; 
			File dir = new File(homeDir);
			
						if(flag ==1){ //check if the query is of short type
							reportFile = similarityType.toString().substring(0, 4)+"shortQuery.txt";
							runvalue = reportFile.substring(0,10);
						}
						else{//if the query is of long type
							reportFile = similarityType.toString().substring(0, 4)+"longQuery.txt";
							runvalue = reportFile.substring(0,10);
						}
						File resultFile = new File (dir, reportFile);
						System.out.println("Output File Name:" +reportFile);
						resultFile.createNewFile();
						PrintWriter writer = new PrintWriter(resultFile, "UTF-8");
						//writer.println("QueryID \t Q0 \t DocID \t \t Rank \t Score \t \t RunID");
						Analyzer analyzer = new StandardAnalyzer();
						QueryParser parser = new QueryParser("TEXT", analyzer);
						IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
						IndexSearcher searcher = new IndexSearcher(reader);
						
						searcher.setSimilarity(similarityType);		//set the similarity type	
						for(int i=0; i<queryArray.size();i++){
								relScoreMap.clear();
								Query query = parser.parse(parser.escape(queryArray.get(i)));
								TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
								searcher.search(query, collector);
								ScoreDoc[] docs = collector.topDocs().scoreDocs;
								for (int i1 = 0; i1 < docs.length; i1++) {
									Document doc = searcher.doc(docs[i1].doc);
									addValues(doc.get("DOCNO"), docs[i1].score); //add (docid, relevance score) to map
								}
							
								List<Entry<String, Float>> sortedMap = entriesSortedByValues(relScoreMap); //sort the map on values
								
							    for(int it = 0,count = 1;it < sortedMap.size() && count <= 1000; it++, count++) {// output first 1000 ranked documents  to the report file
									    	 writer.println(Integer.valueOf(listOfQUERYs.get(i).toString().trim())+" \t\t 0 \t "+sortedMap.get(it).getKey()+" \t "+count+" \t "+sortedMap.get(it).getValue()+" \t "+runvalue);
						        }
						}
						reader.close();
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

		
		public void addValues(String key, float score) {
		float tempValue = 0;
						   if (relScoreMap.containsKey(key)) {// add to previous value of a key if the key already exists in the map.
							   tempValue = relScoreMap.get(key);
							   tempValue += score;
							   relScoreMap.put(key, tempValue);
							   tempValue = 0;
						   } else {
							   relScoreMap.put(key, score);// add to map its a new key,value pair
							   }
						}
						
				
				
}


