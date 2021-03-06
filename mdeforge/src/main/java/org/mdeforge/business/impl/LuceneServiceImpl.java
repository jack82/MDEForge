package org.mdeforge.business.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.mdeforge.business.ATLTransformationService;
import org.mdeforge.business.BusinessException;
import org.mdeforge.business.CRUDArtifactService;
import org.mdeforge.business.EcoreMetamodelService;
import org.mdeforge.business.GridFileMediaService;
import org.mdeforge.business.LuceneService;
import org.mdeforge.business.ModelService;
import org.mdeforge.business.model.ATLTransformation;
import org.mdeforge.business.model.Artifact;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.Model;
import org.mdeforge.business.model.User;
import org.mdeforge.business.model.form.SearchResult;
import org.mdeforge.business.model.form.SearchResultComplete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LuceneServiceImpl implements LuceneService{
	
	
	/*
	 * GENERAL TAGS
	 */
	public static final String TEXT_TAG = "text";
	public static final String TYPE_TAG = "forgeType";
	public static final String NAME_TAG = "name";
	public static final String AUTHOR_TAG = "author";
	public static final String ID_TAG = "id";
	public static final String LAST_UPDATE_TAG = "lastUpdate";
	public static final int TIKA_CHARACTERS_LIMIT = 5000000; // characters
	/*
	 * ECORE METAMODEL TAGS
	 */
	
	public static final String EPACKAGE_INDEX_CODE = "ePackage";
	public static final String NsURI_INDEX_CODE = "nsuri";
	public static final String EANNOTATION_INDEX_CODE = "eAnnotation";
	public static final String ECLASS_INDEX_CODE = "eClass";
	public static final String EATTRIBUTE_INDEX_CODE = "eAttribute";
	public static final String EREFERENCE_INDEX_CODE = "eReference";
	public static final String EENUM_INDEX_CODE = "eEnum";
	public static final String ELITERAL_INDEX_CODE = "eLiteral";
	public static final String EDATATYPE_INDEX_CODE = "eDataType";
	
	public static String[] metamodelLuceneTags = {EPACKAGE_INDEX_CODE,NsURI_INDEX_CODE,EANNOTATION_INDEX_CODE,
			ECLASS_INDEX_CODE,EATTRIBUTE_INDEX_CODE,
			EREFERENCE_INDEX_CODE,EENUM_INDEX_CODE,
			ELITERAL_INDEX_CODE,EDATATYPE_INDEX_CODE};
	
	/*
	 * ATL Transformation TAGS
	 */
	public static final String HELPER_TAG = "helper";
	public static final String FROM_METAMODEL_TAG = "fromMM";
	public static final String TO_METAMODEL_TAG = "toMM";
	public static final String RULE_NAME_TAG = "rule";
	public static final String FROM_METACLASS = "fromMC";
	public static final String FROM_TOCLASS = "toMC";
	
	public static String[] modelLuceneTags = {HELPER_TAG,FROM_METAMODEL_TAG,TO_METAMODEL_TAG,
			RULE_NAME_TAG,FROM_METACLASS,FROM_TOCLASS};
	/*
	 * MODEL TAGS
	 */
	public static final String CUSTOM_LUCENE_INDEX_SEPARATOR_CHARACTER = "_";
	public static final String CONFORM_TO_TAG = "conformToMM";
	public static String[] transformationLuceneTags = {CONFORM_TO_TAG};

	
	
	Logger logger = LoggerFactory.getLogger(CRUDArtifactServiceImpl.class);
	
	@Autowired
	protected GridFileMediaService gridFileMediaService;
	@Autowired
	private EcoreMetamodelService ecoreMetamodelService;
	@Autowired
	private ATLTransformationService aTLTransformationService;
	@Autowired
	private ModelService modelService;
	@Autowired
	private CRUDArtifactService<Artifact> artifactService;
	
	@Value("#{cfgproperties[basePathLucene]}")
	private String basePathLucene;

	@Override
	public void createLuceneIndexFromConfigurationPath() {
		File directoryToIndex = new File(basePathLucene);
		if (directoryToIndex.isDirectory() && directoryToIndex.exists()) {
		
			float duration = 0;
			long startTime = System.nanoTime();
			
			System.out.println("Start Metamodel indexing...");
			metamodelIndex();
			System.out.println("End Metamodel indexing!");
			System.out.println("Start ATL Transformation indexing...");
			atlIndex();
			System.out.println("End ATL Transformation indexing.");
			System.out.println("Start Model indexing...");
			modelIndex();
			System.out.println("End Model indexing.");
			long endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000; // milliseconds(1000000) - seconds (1000000000)
			
			System.out.println("End Overall indexing in " + duration +" ms.");
		}else{
			System.out.println("The provided folder doesn't exists. Check the configuration file.");
		}
	}
	
	private void metamodelIndex() {
		List<EcoreMetamodel> ecoreMMlist = ecoreMetamodelService.findAll();
		for (EcoreMetamodel ecoreMetamodel : ecoreMMlist) {
				System.out.println("Indexing: " + ecoreMetamodel.getName());
//				ecoreMetamodelService.createIndex(ecoreMetamodel);
				ecoreMetamodelService.createLuceneIndex(ecoreMetamodel);
		}
		System.out.println("------------------------------------------------");
		System.out.println("Index of " + ecoreMMlist.size() + " metamodels done!");
		// ecoreMetamodelService.search("eClass:Family");
	}

	
	private void atlIndex() {
		List<ATLTransformation> atlTList = aTLTransformationService.findAll();
		for (ATLTransformation atl : atlTList) {
				System.out.println("Indexing: " + atl.getName());
				aTLTransformationService.createLuceneIndex(atl);
		}
		System.out.println("------------------------------------------------");
		System.out.println("Index of " + atlTList.size() + " metamodels done!");
		// ecoreMetamodelService.search("eClass:Family");
	}
	
	private void modelIndex(){
		List<Model> modelList = modelService.findAll();
		for (Model model : modelList) {
//			if(model.isValid()){
				System.out.println("Indexing: " + model.getName());
				modelService.createLuceneIndex(model);
//			}else{
//				System.out.println(model.getName() + " not a valid model!");
//			}
			
		}
		System.out.println("------------------------------------------------");
		System.out.println("Index of " + modelList.size() + " models done!");
	}
	
	
	
	
	@Override
	public String getMaxFrequencyTerms() throws IOException{
		 IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(basePathLucene)));
	      final Fields fields = MultiFields.getFields(reader);
	      final Iterator<String> iterator = fields.iterator();

	      long maxFreq = Long.MIN_VALUE;
	      String freqTerm = "";
	      while(iterator.hasNext()) {
	          final String field = iterator.next();
	          final Terms terms = MultiFields.getTerms(reader, field);
	          final TermsEnum it = terms.iterator();
	          BytesRef term = it.next();
	         
	          while (term != null) {
	              final long freq = it.totalTermFreq();
	              if (freq > maxFreq) {
	                  maxFreq = freq;
	                  freqTerm = term.utf8ToString();
	              }
//	              if(freq > 1 && term.utf8ToString().length() > 1){
//	            	  System.out.println(freq +"; "+ term.utf8ToString());
//	              }
	              term = it.next();
	          }
	      }

	      System.out.println(freqTerm + " ["+ maxFreq+" occurrencies]");
	      return freqTerm;
	  }
	
	
	/*
	 * SEARCHING
	 */
	@Override
	public SearchResultComplete search(User user, String queryString) throws BusinessException {
		
		SearchResultComplete searchResultComplete = new SearchResultComplete();
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		
		long duration = 0;
		long startTime = System.nanoTime();
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(basePathLucene)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
		
		
			//Get all indexed fields
			String[] fields = artifactService.getAllIndexTags().toArray(new String[0]);
			//Query Parse over multiple Indexed Fields
			
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			TopDocs results = searcher.search(query, Integer.MAX_VALUE);
//			ScoreDoc[] hits = results.scoreDocs;
			int numTotalHits = results.totalHits;
			System.out.println(numTotalHits + " total matching documents");

			for (int i = 0; i < numTotalHits; i++) {
				int id = results.scoreDocs[i].doc;
				Document doc = searcher.doc(id);

				Artifact art = artifactService.findOne(doc.get(LuceneServiceImpl.ID_TAG));
				if(isArtifactAvailableForUser(art, user)){
					SearchResult sr = new SearchResult();
					sr.setArtifact(art);
					sr.setScore(results.scoreDocs[i].score);
					searchResults.add(sr);
				}
			}
			
			reader.close();
			
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		} catch (ParseException e) {
			throw new BusinessException(e.getMessage());
		} 
		
		
		long endTime = System.nanoTime();
		duration = (endTime - startTime)/1000000; //milliseconds(1000000) - seconds (1000000000)
//		System.out.println("Search done in " + duration + " milliseconds");
		
		searchResultComplete.setResults(searchResults);
		searchResultComplete.setSearchTime(duration);
		
		return searchResultComplete;
	}
	
	

	
	@Override
	public List<Artifact> search(User user, String queryString, int maxSearchResult) throws BusinessException {
		List<Artifact> listArtifact = new ArrayList<Artifact>();
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(basePathLucene)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			//Get all indexed fields
			String[] fields = artifactService.getAllIndexTags().toArray(new String[0]);
			//Query Parse over multiple Indexed Fields
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			TopDocs hits = searcher.search(query, maxSearchResult);
			int max = (maxSearchResult > hits.totalHits)?hits.totalHits:maxSearchResult;
			for (int i = 0; i < max; i++) {
//				try {
					int id = hits.scoreDocs[i].doc;
					Document doc = searcher.doc(id);
					Artifact art = artifactService.findOne(doc.get(LuceneServiceImpl.ID_TAG));
					if(isArtifactAvailableForUser(art, user)){
						listArtifact.add(art);
					}
//				} catch (Exception e){}
			}
			reader.close();
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		} catch (ParseException e1) {
			throw new BusinessException(e1.getMessage());
		} 

		return listArtifact;
	}
	
	
	@Override
	public SearchResultComplete search(User user, String queryString, int hitsPerPage, int pageNumber) throws BusinessException {
		long duration = 0;
		long startTime = System.nanoTime();
		
		int numTotalHits;
		
		SearchResultComplete searchResultComplete = new SearchResultComplete();
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(basePathLucene)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			
			//Get all indexed fields
			String[] fields = artifactService.getAllIndexTags().toArray(new String[0]);
			//Query Parse over multiple Indexed Fields
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
			org.apache.lucene.search.Query query = queryParser.parse(queryString);
			
			// Collect enough docs to show x (pageNumber) pages
		    TopDocs results = searcher.search(query, pageNumber * hitsPerPage);
		    ScoreDoc[] hits = results.scoreDocs;
			
		    numTotalHits = results.totalHits;
		    System.out.println(numTotalHits + " total matching documents");
		    
//		    int start = 0;
		    int start = (pageNumber-1) * hitsPerPage;;
		    int end = Math.min(numTotalHits, hitsPerPage);
		    
		    if(end > hits.length){
		    	hits = searcher.search(query, numTotalHits).scoreDocs;
		    }
			
		    end = Math.min(hits.length, start + hitsPerPage);
		    
		    for (int i = start; i < end; i++) {
		    	Document doc = searcher.doc(hits[i].doc);
		    	Artifact art = artifactService.findOne(doc.get(LuceneServiceImpl.ID_TAG));
				//it search only public artifacts or private or shared with user logged in
				if(isArtifactAvailableForUser(art, user)){
					SearchResult sr = new SearchResult();
					sr.setArtifact(art);
					sr.setScore(hits[i].score);
					searchResults.add(sr);
				}
				
		    }
		    
		    long endTime = System.nanoTime();
			duration = (endTime - startTime)/1000000; //milliseconds(1000000) - seconds (1000000000)
		    
		    reader.close();
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		} catch (ParseException e) {
			throw new BusinessException(e.getMessage());
		} 
		
		searchResultComplete.setResults(searchResults);
		searchResultComplete.setSearchTime(duration);
		searchResultComplete.setTotalHits(numTotalHits);
		searchResultComplete.setHitsPerPage(hitsPerPage);
		searchResultComplete.setPageNumber(pageNumber);
		searchResultComplete.setQueryString(queryString);
		int numberOfPages = 0;
		if(numTotalHits % hitsPerPage == 0){
			numberOfPages = numTotalHits/hitsPerPage;
		}else{
			numberOfPages = (numTotalHits/hitsPerPage) + 1;
		}
		searchResultComplete.setPages(numberOfPages);
		
		return searchResultComplete;
	}
	private boolean isArtifactAvailableForUser(Artifact artifact, User user){
		boolean result = false;
		if(user != null){
			//If we are searching in the private area (a User is logged into the system)
			if(artifact.isOpen() || artifact.getAuthor().equals(user) || artifact.getShared().contains(user)){
				result = true;
			}
		}else{
			//We are searching in the public area (only public artifact have to be shown)
			if(artifact.isOpen()){
				result = true;
			}
		}
		
		
		return result;
	}
}
