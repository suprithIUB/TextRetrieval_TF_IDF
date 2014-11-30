import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;



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
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.packed.PackedInts.Reader;



public class easySearch {
	public String pathToIndex = null; 
	public String queryString = null;
	Map<String,Double> relScoreMap=new TreeMap<String, Double>();
	easySearch(String pathName, String queryString){
		this.pathToIndex = pathName;
		this.queryString = queryString;
	}

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * 
	 */
	/*
	 * addValues - checks for duplicate keys and adds to the previous value in tree map.
	 * input - key, value
	 * 
	 */
	
	public void addValues(String key, Double value) {
		   Double tempValue = 0.0;
		   if (relScoreMap.containsKey(key)) {// add to previous value of a key if the key already exists in the map.
			   tempValue = relScoreMap.get(key);
			   tempValue += value;
			   relScoreMap.put(key, tempValue);
			   tempValue = 0.0;
		   } else {
			   relScoreMap.put(key, value);// add to map its a new key,value pair
			   }
		}
	
	
	
	/* computeRelScore
	 * Calculates relevance score for every query term and outputs the first 1000 relevant documents to a text file.
	 * Input parameters: List of queries, list of query numbers, ranking algorithm type and flag.
	 */
	public void computeRelScore() throws ParseException, IOException{
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		Integer docCount;
		double relevanceFactor = 0;
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		System.out.println("Total Number of Documents in the corpus: " +reader.maxDoc());
		int numOfDocsInCorpus = reader.maxDoc();
		query.extractTerms(queryTerms);
		for (Term t : queryTerms) { // for all query terms
				System.out.println("Query term: " +t.text()); 
				docCount = reader.docFreq(t);
				System.out.println("Number of docs matching the query term: "+ t.text() +" is:" +docCount);
				if(docCount > 0) //find if the docs contain the given query term
				{
					double IDF = Math.log10(1+(numOfDocsInCorpus/docCount)); // compute IDF for the query term
					System.out.println("IDF: " +IDF);
					DefaultSimilarity dSimi=new DefaultSimilarity();
					List<AtomicReaderContext> leafContexts = reader.getContext().reader().leaves();
					for (int i = 0; i < leafContexts.size(); i++) { //for every leaf in the index
							AtomicReaderContext leafContext=leafContexts.get(i);
							int startDocNo=leafContext.docBase;
							int numberOfDoc=leafContext.reader().maxDoc(); 
							DocsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),MultiFields.getLiveDocs(leafContext.reader()),"TEXT", new BytesRef(t.text())); // get list of docs matching a query term in a leaf
							int doc;
							
							while (de!= null && (doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) { // for every relevant doc
										
											Document docu = searcher.doc(de.docID()+startDocNo);
											String DocID = docu.get("DOCNO");
											float normDocLeng=dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(doc)); // normalized length of the doc
											double normalizedTermFrequency = de.freq()/normDocLeng; //compute term frequency
											relevanceFactor = normalizedTermFrequency * IDF; // relevance factor for the doc
											System.out.println("Relevance Factor for query term: \"" +t.text()+ "\" for doc: "+(doc+startDocNo) +" is --->: " +relevanceFactor);
											addValues(DocID, relevanceFactor); //add the (docid,relevance score) pair to map
							}
							 
					}
				}
				else{
						System.out.println("Search term doesn't exist in the index vocabulary: " +t.text());
				}
				
		}
		System.out.println("***************************************************************");
		System.out.println("Relevance Scores for all the relevant docs in the corpus for the given query is listed below");
		System.out.println(relScoreMap);
	    reader.close();
}
	
	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
		try{ 
				Scanner consoleInput = new Scanner(System.in); // scanners for fetching user input
				System.out.println("Enter the path to index dir");
				
				String pathToIndex = consoleInput.nextLine();	
				System.out.println("Enter the query string");
				
				String queryString = consoleInput.nextLine();	
				System.out.println("Computing the relevance scores based on the given input");
				easySearch baby = new easySearch(pathToIndex, queryString); 
				baby.computeRelScore(); //calling computeRelScore method which initiates the search algorithm
				
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
	


