package org.mdeforge.business.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IExtractor;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFExtractor;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFReferenceModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.OCLHelper;
import org.emfjson.jackson.module.EMFModule;
import org.mdeforge.business.BusinessException;
import org.mdeforge.business.EcoreMetamodelService;
import org.mdeforge.business.InvalidArtifactException;
import org.mdeforge.business.ValuedRelationService;
import org.mdeforge.business.importer.impl.EcoreMetamodelImporterServiceImpl;
import org.mdeforge.business.model.AggregatedIntegerMetric;
import org.mdeforge.business.model.AggregatedRealMetric;
import org.mdeforge.business.model.Artifact;
import org.mdeforge.business.model.Cluster;
import org.mdeforge.business.model.Clusterizzation;
import org.mdeforge.business.model.ContainmentRelation;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.Metric;
import org.mdeforge.business.model.Property;
import org.mdeforge.business.model.Relation;
import org.mdeforge.business.model.SimilarityRelation;
import org.mdeforge.business.model.SimpleMetric;
import org.mdeforge.business.model.ToBeAnalyse;
import org.mdeforge.business.model.ValuedRelation;
import org.mdeforge.business.model.form.Statistic;
import org.mdeforge.emf.metric.Container;
import org.mdeforge.emf.metric.MetricFactory;
import org.mdeforge.emf.metric.MetricPackage;
import org.mdeforge.integration.EcoreMetamodelRepository;
import org.mdeforge.integration.MetricRepository;
import org.mdeforge.integration.ToBeAnalyseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import com.apporiented.algorithm.clustering.visualization.DendrogramPanel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import anatlyzer.atl.util.ATLSerializer;
import anatlyzer.atlext.ATL.ATLPackage;
import anatlyzer.atlext.OCL.OclExpression;

@Service
public class EcoreMetamodelServiceImpl extends CRUDArtifactServiceImpl<EcoreMetamodel>
		implements EcoreMetamodelService {


	private IndexWriter writer;
	
	@Autowired
	private SimpleMongoDbFactory mongoDbFactory;
	@Autowired
	private EcoreMetamodelRepository ecoreMetamodelRepository;
	@Autowired
	private MetricRepository metricRepository;
	@Autowired
	private ToBeAnalyseRepository toBeAnalyseRepository;

	// @Value("#{cfgproperties[basePathLucene]}")
	// protected String basePathLucene;
	// @Value("#{cfgproperties[basePath]}")
	// protected String basePath;
	Logger logger = LoggerFactory.getLogger(EcoreMetamodelImporterServiceImpl.class);
	@Value("#{cfgproperties[mongoPrefix]}")
	private String mongoPrefix;
	@Value("#{cfgproperties[jsonArtifactCollection]}")
	private String jsonArtifactCollection;

	@Override
	public EcoreMetamodel create(EcoreMetamodel artifact) {
		EcoreMetamodel result = super.create(artifact);
		try {
			result.setValid(isValid(result));
		} catch (Exception e) {
			logger.error("Validation error");
		}
		try {
			result.getUri().addAll(getNSUris(result));
		}catch(Exception e){
			logger.error("Unable to extract nsURIs");
		}
//		try {
//			this.extractedContent(result);
//		} catch (Exception e) {
//			logger.error("Some errors when try to extract content string from metamodel.");
//		}
		try {
			result.setMetrics(calculateMetrics(result));
		} catch (Exception e) {
			logger.error("Some errors when try to calculate metrics for metamodel.");
		}
		artifactRepository.save(result);
		try {
			createLuceneIndex(result);
		} catch (Exception e) {
			logger.error("Some errors when try to create lucene index.");
		}
		artifactRepository.save(artifact);
		return result;
	}

	private Document parseMetamodelForIndex(EcoreMetamodel ecoreMetamodel) {
		Document doc = new Document();
		
		//Register Metamodel
		String artifactFilePath = "";
		try{
			artifactFilePath = gridFileMediaService.getFilePath(ecoreMetamodel);
		}catch (Exception e) {
			 System.err.println("File not exists!");
		}
		URI fileURI = URI.createFileURI(artifactFilePath);
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new EcoreResourceFactoryImpl());
		
		ResourceSet resourceSet = new ResourceSetImpl();
		try{
			Resource resource = resourceSet.getResource(fileURI, true);	
		if (resource.isLoaded() && resource.getErrors() != null) {
			TreeIterator<EObject> eAllContents = resource.getAllContents();
			while (eAllContents.hasNext()) {
				EObject next = eAllContents.next();
				if (next instanceof EPackage) {
					EPackage ePackage = (EPackage) next;
					doc = ePackageIndex(ePackage, doc);
				} else if (next instanceof EClass) {
					EClass eClass = (EClass) next;
					doc = eClassIndex(eClass, doc);
				} else if (next instanceof EEnum) {
					EEnum eEnum = (EEnum) next;
					doc = eEnumIndex(eEnum, doc);
				} else if (next instanceof EDataType) {
					EDataType eDataType = (EDataType) next;
					doc = eDataTypeIndex(eDataType, doc);
				} else if (next instanceof EAnnotation) {
					// GET all the EAnnotations
					EList<EAnnotation> annotations = ((EModelElement) next).getEAnnotations();
					doc = indexAnnotations(annotations, doc);
				}

			}
		}
		
		//ID
		Field idField = new TextField(LuceneServiceImpl.ID_TAG, ecoreMetamodel.getId(), Field.Store.YES);
	 	doc.add(idField);
		
		//Artifact TYPE: "EcoreMetamodel"
		Field artifactType = new TextField(LuceneServiceImpl.TYPE_TAG, ecoreMetamodel.getClass().getSimpleName(), Field.Store.YES);
		doc.add(artifactType);
		
//		String text = handler.toString();
		String text = getTextFromInputStream(gridFileMediaService.getFileInputStream(ecoreMetamodel));
		Field textField = new TextField(LuceneServiceImpl.TEXT_TAG, text, Field.Store.YES);
		doc.add(textField);

		
		
		Field artName = new TextField(LuceneServiceImpl.NAME_TAG, ecoreMetamodel.getName(), Field.Store.YES);
	 	doc.add(artName);
	 	
	 	Field authorField = new TextField(LuceneServiceImpl.AUTHOR_TAG, ecoreMetamodel.getAuthor().getUsername(), Field.Store.YES);
	 	doc.add(authorField);
	 	
	 	Field lastUpdateField = new TextField(LuceneServiceImpl.LAST_UPDATE_TAG, ecoreMetamodel.getModified().toString(), Field.Store.YES);
	 	doc.add(lastUpdateField);
	 	
	 	for (Property prop : ecoreMetamodel.getProperties()) {
			String propName = prop.getName();
			String propValue = prop.getValue();
			if(propName != null && propValue != null) {
				Field propField = new TextField(propName, propValue, Field.Store.YES);
				doc.add(propField);
			}
		}
	 	
		}catch (Exception e) {
			System.out.println("ERROR");
		}
//		System.out.println(handler.toString());	 	

	 	return doc;
	}
	
	private Document ePackageIndex(EPackage ePackage, Document doc){
		Field ePackageField = new TextField(LuceneServiceImpl.EPACKAGE_INDEX_CODE, ePackage.getName(), Field.Store.YES);
		doc.add(ePackageField);
		// GET NsURI
		if (ePackage.getNsURI() != null && !ePackage.getNsURI().isEmpty()) {
			Field EPackageNsURIField = new TextField(LuceneServiceImpl.NsURI_INDEX_CODE, ePackage.getNsURI(), Field.Store.YES);
//			System.out.println("NsURI : " + ePackage.getNsURI());
			doc.add(EPackageNsURIField);
		}
		// GET EAnnotation
		EList<EAnnotation> annotations = ePackage.getEAnnotations();
		if (annotations != null && !annotations.isEmpty()) {
			doc = indexAnnotations(annotations, doc);
		}
		
		return doc;
	}
	

	
	private Document eClassIndex(EClass eClass, Document doc){
		try {
			Field eClassField = new TextField(LuceneServiceImpl.ECLASS_INDEX_CODE, eClass.getName(), Field.Store.YES);
			// System.out.println("Class: " + eClass.getName());
			doc.add(eClassField);

			// GET EAnnotation
			EList<EAnnotation> annotations = eClass.getEAnnotations();
			if (annotations != null && !annotations.isEmpty()) {
				doc = indexAnnotations(annotations, doc);
			}

			// Index EClass Attributes
			for (EAttribute attribute : eClass.getEAttributes()) {
				Field eClassAttributeField = new TextField(LuceneServiceImpl.EATTRIBUTE_INDEX_CODE, attribute.getName(), Field.Store.YES);
				// System.out.println("Attribute: " + attribute.getName());
				doc.add(eClassAttributeField);
			}
			// Index EClass References
			for (EReference reference : eClass.getEReferences()) {
				Field eClassReferenceField = new TextField(LuceneServiceImpl.EREFERENCE_INDEX_CODE, reference.getName(), Field.Store.YES);
				// System.out.println("Reference: " + reference.getName());
				doc.add(eClassReferenceField);
			}
		
		} catch (Exception e) {
			System.err.println("ERROR");
		}
		
		return doc;
	}
	
	/**
	 * Index the annotation list provided as input.
	 * @param annotations
	 * @param doc
	 * @return Document
	 */
	private Document indexAnnotations(List<EAnnotation> annotations, Document doc){
		if (annotations != null && !annotations.isEmpty()) {
			for (EAnnotation eAnnotation : annotations) {
				if(getAnnotationKey(eAnnotation) != null && getAnnotationKey(eAnnotation).equals("weight")){
					if(getAnnotationValue(eAnnotation) != null){
						Field EPackageEAnnotationField = new TextField(LuceneServiceImpl.EANNOTATION_INDEX_CODE, getAnnotationValue(eAnnotation), Field.Store.YES);
						doc.add(EPackageEAnnotationField);
					}
				}
			}
		}
		return doc;
	}
	
	
	/**
	 * Get the annotation key from an EAnnotation
	 * @param eAnnotation
	 * @return String
	 */
	private String getAnnotationKey(EAnnotation eAnnotation){
		String result = null;
		if (eAnnotation != null) {
				EMap<String, String> annotationDetails = eAnnotation.getDetails();
				for (Entry<String, String> entry : annotationDetails) {
					if(entry.getKey() != null && entry.getValue() != null){
						result = entry.getKey();
				}
			}
		}
		return result;
	}
	
	/**
	 * Get the annotation value from an EAnnotation
	 * @param eAnnotation
	 * @return String
	 */
	private String getAnnotationValue(EAnnotation eAnnotation){
		String result = null;
		if (eAnnotation != null) {
			EMap<String, String> annotationDetails = eAnnotation.getDetails();
			for (Entry<String, String> entry : annotationDetails) {
				if(entry.getKey() != null && entry.getValue() != null){
					result = entry.getValue();
				}
			}
		}
		return result;
	}
	
	private Document eEnumIndex(EEnum eEnum, Document doc){
		Field eEnumField = new TextField(LuceneServiceImpl.EENUM_INDEX_CODE, eEnum.getName(), Field.Store.YES);
		doc.add(eEnumField);
		return doc;
	}
	
	private Document eDataTypeIndex(EDataType eDataType, Document doc){
		Field eDataTypeField = new TextField(LuceneServiceImpl.EDATATYPE_INDEX_CODE, eDataType.getName(), Field.Store.YES);
		doc.add(eDataTypeField);
		return doc;
	}
	
	@Override
	public void createLuceneIndex(EcoreMetamodel is) {

		try {
			Directory indexDir = FSDirectory.open(Paths.get(basePathLucene));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			// Create an index in the directory, appending new index over
			// previously indexed documents:
			conf.setOpenMode(OpenMode.CREATE_OR_APPEND); // or CREATE
			// create the indexer
			Document document = parseMetamodelForIndex(is);
			writer = new IndexWriter(indexDir, conf);

			// writer.updateDocument(new Term("path", file.getPath()),
			// document);
			writer.addDocument(document);

			writer.close();
		} catch (CorruptIndexException e) {
			throw new BusinessException(e.getMessage());
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		}

	}

	@Override
	public List<EcoreMetamodel> findByURI(String URI) {
		MongoOperations n = new MongoTemplate(mongoDbFactory);
		org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
		Criteria c1 = Criteria.where("uri").is(URI);
		query.addCriteria(c1);
		return n.find(query, EcoreMetamodel.class);
	}

	// @Override
	// public ResponseGrid<EcoreMetamodel> findAllEcorePaginated(
	// RequestGrid requestGrid) throws BusinessException {
	// Page<EcoreMetamodel> rows = null;
	// if (requestGrid.getSortDir().compareTo("asc") == 0) {
	// rows = ecoreMetamodelRepository.findByOpen(
	// true,
	// new PageRequest(requestGrid.getiDisplayStart()
	// / requestGrid.getiDisplayLength(), requestGrid
	// .getiDisplayLength(), Direction.ASC, requestGrid
	// .getSortCol()));
	// } else {
	// rows = ecoreMetamodelRepository.findByOpen(
	// true,
	// new PageRequest(requestGrid.getiDisplayStart()
	// / requestGrid.getiDisplayLength(), requestGrid
	// .getiDisplayLength(), Direction.DESC, requestGrid
	// .getSortCol()));
	// }
	// return new ResponseGrid<EcoreMetamodel>(requestGrid.getsEcho(),
	// rows.getNumberOfElements(), rows.getTotalElements(),
	// rows.getContent());
	// }

	@Override
	public List<Metric> calculateMetrics(Artifact emm) throws BusinessException {
		ILauncher transformationLauncher = new EMFVMLauncher();
		ModelFactory modelFactory = new EMFModelFactory();
		IInjector injector = new EMFInjector();
		IExtractor extractor = new EMFExtractor();
		/*
		 * Load metamodels
		 */
		try {
			IReferenceModel outputMetamodel = modelFactory.newReferenceModel();
			injector.inject(outputMetamodel, getClass().getResource("/utils/Metric.ecore").getFile());
			IReferenceModel inputMetamodel = modelFactory.newReferenceModel();
			injector.inject(inputMetamodel, org.eclipse.emf.ecore.EcorePackage.eNS_URI);
			IModel inputModel = modelFactory.newModel(inputMetamodel);
			IModel outModel = modelFactory.newModel(outputMetamodel);
			injector.inject(inputModel, gridFileMediaService.getFileInputStream(emm), null);
			transformationLauncher.initialize(new HashMap<String, Object>());
			transformationLauncher.addInModel(inputModel, "IN", "Ecore");
			transformationLauncher.addOutModel(outModel, "OUT", "Metric");
			transformationLauncher.launch(ILauncher.RUN_MODE, null, new HashMap<String, Object>(),
					(Object[]) getModulesList(getClass().getResource("/utils/EcoreMetric.asm").getFile()));
			extractor.extract(outModel, "sampleCompany_Cut.xmi");
			EMFModelFactory emfModelFactory = (EMFModelFactory) modelFactory;
			emfModelFactory.unload((EMFReferenceModel) inputMetamodel);
			emfModelFactory.unload((EMFReferenceModel) outputMetamodel);
			List<Metric> result = getMetricList("sampleCompany_Cut.xmi", emm);
			File temp2 = new File("sampleCompany_Cut.xmi");
			temp2.delete();
			metricRepository.save(result);
			return result;
		} catch (ATLCoreException e) {
			throw new BusinessException(e.getMessage());
		} catch (IOException e) {
			throw new BusinessException(e.getMessage());
		}
		// List<Metric> a = new ArrayList();
		// return a;
	}



	private InputStream[] getModulesList(String modules_input) throws IOException {
		InputStream[] modules = null;
		String[] moduleNames = modules_input.split(",");
		modules = new InputStream[moduleNames.length];
		for (int i = 0; i < moduleNames.length; i++) {
			String asmModulePath = new Path(moduleNames[i].trim()).removeFileExtension().addFileExtension("asm")
					.toString();
			modules[i] = new FileInputStream(asmModulePath);
		}
		return modules;
	}

	private List<Metric> getMetricList(String path, Artifact art) {
		MetricPackage.eINSTANCE.eClass();
		// Retrieve the default factory singleton
		@SuppressWarnings("unused")
		MetricFactory factory = MetricFactory.eINSTANCE;
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		// Obtain a new resource set
		ResourceSet resSet = new ResourceSetImpl();
		// Create a resource
		Resource resource = resSet.createResource(URI.createURI(path));
		try {
			resource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Container myForge = (Container) resource.getContents().get(0);
		List<Metric> result = new ArrayList<Metric>();
		Iterator<org.mdeforge.emf.metric.Metric> it = myForge.getMetrics().iterator();
		while (it.hasNext()) {
			org.mdeforge.emf.metric.Metric at2 = (org.mdeforge.emf.metric.Metric) it.next();
			Metric metric = null;
			if (at2 instanceof org.mdeforge.emf.metric.impl.SimpleMetricImpl) {
				SimpleMetric metric2 = new SimpleMetric();
				metric2.setName(at2.getName());

				metric2.setDescription(at2.getDescription());
				metric2.setValue(((org.mdeforge.emf.metric.impl.SimpleMetricImpl) at2).getValue());
				metric = metric2;
			}
			if (at2 instanceof org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) {
				AggregatedIntegerMetric metric2 = new AggregatedIntegerMetric();
				metric2.setAverage(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2).getAverage());
				metric2.setMaximum(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2).getMaximum());
				metric2.setMedian(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2).getMedian());
				metric2.setMinimum(((org.mdeforge.emf.metric.impl.AggregatedIntegerMetricImpl) at2).getMinimum());
				metric2.setName(at2.getName());
				metric2.setDescription(at2.getDescription());
				metric = metric2;
			}
			if (at2 instanceof org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) {
				AggregatedRealMetric metric2 = new AggregatedRealMetric();
				metric2.setAverage(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2).getAverage());
				metric2.setMaximum(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2).getMaximum());
				metric2.setMedian(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2).getMedian());
				metric2.setMinimum(((org.mdeforge.emf.metric.impl.AggregatedRealMetricImpl) at2).getMinimum());
				metric2.setName(at2.getName());
				metric2.setDescription(at2.getDescription());
				metric = metric2;
			}
			metric.setArtifact(art);
			// metricRepository.save(metric);
			result.add(metric);
		}
		return result;
	}

	@Override
	public Resource registerMetamodel(EcoreMetamodel ecoreMetamodel) throws BusinessException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

		ResourceSet rs = new ResourceSetImpl();
		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);

		Resource r = rs.getResource(URI.createFileURI(gridFileMediaService.getFilePath(ecoreMetamodel)), true);
		for (EObject eObject : r.getContents()) {
			if (eObject instanceof EPackage) {
				EPackage p = (EPackage) eObject;
				registerSubPackage(p);
			}
		}
		
		return r;
	}

	@Override
	public List<EPackage> getEPackageList(EcoreMetamodel ecoreMetamodel) throws BusinessException {
		ecoreMetamodel = ecoreMetamodelRepository.findOne(ecoreMetamodel.getId());

		ecoreMetamodel.setFile(gridFileMediaService.getGridFileMedia(ecoreMetamodel.getFile()));
		String path = gridFileMediaService.getFilePath(ecoreMetamodel);
		File fileName = new File(path);
		URI uri = URI.createFileURI(fileName.getAbsolutePath());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();
		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
		Resource r = rs.getResource(uri, true);
		try {
			r.load(gridFileMediaService.getFileInputStream(ecoreMetamodel), new HashMap<>());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		List<EObject> eObject = r.getContents();
		List<EPackage> pack = new ArrayList<EPackage>();
		for (EObject eObject2 : eObject) {
			if (eObject2 instanceof EPackage) {
				EPackage p = (EPackage) eObject2;
				pack.add(p);
				registerSubPackage(p);
			}
		}
		return pack;
	}

	@Override
	public void registerMetamodel(String ecoreMetamodel) throws BusinessException {
		String path = ecoreMetamodel;
		File fileName = new File(path);
		URI uri = URI.createFileURI(fileName.getAbsolutePath());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();

		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
		Resource r = rs.getResource(uri, true);
		List<EObject> eObject = r.getContents();
		for (EObject eObject2 : eObject) {
			if (eObject2 instanceof EPackage) {
				EPackage p = (EPackage) eObject2;
				registerSubPackage(p);
			}
		}
	}

	@Override
	public List<String> getNSUris(EcoreMetamodel ecoreMetamodel) throws BusinessException {
		List<String> result = new ArrayList<String>();
		ecoreMetamodel = ecoreMetamodelRepository.findOne(ecoreMetamodel.getId());
		ecoreMetamodel.setFile(gridFileMediaService.getGridFileMedia(ecoreMetamodel.getFile()));
		String path = gridFileMediaService.getFilePath(ecoreMetamodel);
		File fileName = new File(path);
		URI uri = URI.createFileURI(fileName.getAbsolutePath());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		ResourceSet rs = new ResourceSetImpl();
		// enable extended metadata
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
		rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
		Resource r = rs.getResource(uri, true);
		List<EObject> eObject = r.getContents();
		for (EObject eObject2 : eObject) {
			if (eObject2 instanceof EPackage) {
				EPackage p = (EPackage) eObject2;
				String s = p.getNsURI();
				if (s != null && !s.isEmpty())
					result.add(p.getNsURI());
				result.addAll(getNSUris(p));
			}
		}
		return result;
	}

	private List<String> getNSUris(EPackage p) {
		List<String> result = new ArrayList<String>();
		EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		for (EPackage pack : p.getESubpackages()) {
			String s = pack.getNsURI();
			if (s != null && !s.isEmpty())
				result.add(pack.getNsURI());
			result.addAll(getNSUris(pack));
		}
		return result;
	}

	private void registerSubPackage(EPackage p) {
		EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		for (EPackage pack : p.getESubpackages()) {
			registerSubPackage(pack);
		}
	}

	@Override
	public boolean isValid(Artifact art) {
		if (art instanceof EcoreMetamodel) {
			try {
				@SuppressWarnings("unused")
				EcoreFactory factory = EcoreFactory.eINSTANCE;
				ResourceSet resourceSet = new ResourceSetImpl();
				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
						new EcoreResourceFactoryImpl());
				File temp = new File(gridFileMediaService.getFilePath(art));
				Resource resource = resourceSet.createResource(URI.createFileURI(temp.getAbsolutePath()));
				resource.load(null);
				EcoreUtil.resolveAll(resourceSet);
				EObject eo = resource.getContents().get(0);
				Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eo);
				if (diagnostic.getSeverity() == Diagnostic.ERROR){
					return false;
				}
				else
					return true;
			} catch (Exception e) {
				return false;
			}
		} else
			return false;
	}

	public double calculateSimilarity2(Artifact art1, Artifact art2) {
		URI uri1 = URI.createFileURI(gridFileMediaService.getFilePath(art1));
		URI uri2 = URI.createFileURI(gridFileMediaService.getFilePath(art2));
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(uri1, true);
		resourceSet2.getResource(uri2, true);
		IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, null);
		int total = 0;
		try {
			Comparison comparisonDef = EMFCompare.builder().build().compare(scope);
			List<Match> matchesDef = comparisonDef.getMatches();
			int counterDef = 0;
			for (Match match : matchesDef) {
				List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
				total += lm.size();
				for (Match match2 : lm) {
					if (match2.getLeft() != null && match2.getRight() != null)
						counterDef++;
				}
				if (match.getLeft() != null && match.getRight() != null)
					counterDef++;
			}
			SimilarityRelation smr = new SimilarityRelation();
			smr.setFromArtifact(art1);
			smr.setToArtifact(art2);
			smr.setValue((counterDef * 1.0) / total);
			relationRepository.save(smr);
			return smr.getValue();
		} catch (Exception e) {
			SimilarityRelation smr = new SimilarityRelation();
			smr.setFromArtifact(art1);
			smr.setToArtifact(art2);
			smr.setValue(0);
			relationRepository.save(smr);
			return 0;
		}
	}

	@Override
	public double calculateSimilarity(Artifact art1, Artifact art2) {
		// try {
		URI uri1 = URI.createFileURI(gridFileMediaService.getFilePath(art1));
		URI uri2 = URI.createFileURI(gridFileMediaService.getFilePath(art2));
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(uri1, true);
		resourceSet2.getResource(uri2, true);
		IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		List<Match> matches = comparison.getMatches();
		int total = matches.size();
		int counter = 0;
		int counterLeft = 0;
		int counterRight = 0;
		for (Match match : matches) {
			List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
			total += lm.size();
			for (Match match2 : lm) {
				if (match2.getLeft() != null)
					counterLeft++;
				if (match2.getRight() != null)
					counterRight++;
				if (match2.getLeft() != null && match2.getRight() != null)
					counter++;
			}
			if (match.getLeft() != null && match.getRight() != null)
				counter++;
		}
		// to save diff file
		// List<Diff> differences = comparison.getDifferences();
		// // Let's merge every single diff
		// // IMerger.Registry mergerRegistry = new IMerger.RegistryImpl();
		// IMerger.Registry mergerRegistry = IMerger.RegistryImpl
		// .createStandaloneInstance();
		// IBatchMerger merger = new BatchMerger(mergerRegistry);
		// merger.copyAllLeftToRight(differences, new BasicMonitor());
		double containmentValue = (counter * 1.0) / ((counterLeft < counterRight) ? counterLeft : counterRight);
		double simValue = (counter * 1.0) / total;
		// Used to save Diff model
		// Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		// Map<String, Object> m = reg.getExtensionToFactoryMap();
		// m.put("xmi", new XMIResourceFactoryImpl());
		// ResourceSet resSet = new ResourceSetImpl();
		// // create a resource
		// Resource resource =
		// resSet.createResource(URI.createURI("/compare.xmi"));
		// resource.getContents().add(comparison);
		// // try {
		// resource.save(Collections.EMPTY_MAP);
		// } catch (IOException e) {
		// e.printStackTrace();
		// throw new BusinessException();
		// }
		if (simValue > 0) {
			SimilarityRelation sr = new SimilarityRelation();
			sr.setFromArtifact(art1);
			sr.setToArtifact(art2);
			sr.setValue(simValue);
			relationRepository.save(sr);
		}
		if (containmentValue > 0) {
			ContainmentRelation cr = new ContainmentRelation();
			cr.setFromArtifact(art1);
			cr.setToArtifact(art2);
			cr.setValue(containmentValue);
			relationRepository.save(cr);
		}

//		EcoreMetamodel emm1 = (EcoreMetamodel) art1;
//		EcoreMetamodel emm2 = (EcoreMetamodel) art2;
//		String test = serializeContent(emm1);
//		String test2 = serializeContent(emm2);
//		double cosineSimScore = new SimilarityMethods().cosineSimilarityScore(test, test2);
//		if (cosineSimScore > 0) {
//			CosineSimilarityRelation csr = new CosineSimilarityRelation();
//			csr.setFromArtifact(art1);
//			csr.setToArtifact(art2);
//			csr.setValue(cosineSimScore);
//			relationService.save(csr);
//		}
//		DiceSimilarity ds = new DiceSimilarity();
//		double diceSimScore = ds.getSimilarity(test, test2);
//		if (diceSimScore > 0) {
//			DiceSimilarityRelation dsr = new DiceSimilarityRelation();
//			dsr.setFromArtifact(art1);
//			dsr.setToArtifact(art2);
//			dsr.setValue(diceSimScore);
//			relationService.save(dsr);
//		}
		return simValue;
	}

	// region Cluster
	@Override
	public String getSimilarityGraph(double threshold, ValuedRelationService valuedRelationService)
			throws BusinessException {
		try {

			String result = "nodes = [\n";
			List<Cluster> clusterList = getSimilarityClusters(threshold, valuedRelationService).getClusters();
			HashMap<String, Integer> hm = new HashMap<String, Integer>();
			AtomicInteger i = new AtomicInteger();
			AtomicInteger j = new AtomicInteger();

			for (Cluster cluster : clusterList) {
				int groupId = j.incrementAndGet();
				for (Artifact ecoreMetamodel : cluster.getArtifacts()) {
					if (!hm.containsKey(ecoreMetamodel.getId())) {
						int unique = i.incrementAndGet();
						hm.put(ecoreMetamodel.getId(), unique);
					}
					result += "\t{id: " + hm.get(ecoreMetamodel.getId()) + ", label:'" + ecoreMetamodel.getName()
							+ "', group:" + groupId + "},\n";
				}
			}

			result = result.substring(0, result.length() - 2);
			result += "];\n";
			result += "edges = [\n";
			List<ValuedRelation> relations = valuedRelationService.findAll(threshold);
			int size = relations.size();
			for (ValuedRelation relation : relations) {
				Double d = (relation.getValue() * 10);
				String s = relation.getValue() + "";
				s = (s.length() < 5) ? s : s.substring(0, 5);
				result += "{from:" + hm.get(relation.getFromArtifact().getId()) + ", to: "
						+ hm.get(relation.getToArtifact().getId()) + ", value: " + d.intValue() + ", label:" + s + "}";
				if (--size != 0)
					result += ",\n";
				else
					result += "\n";
			}
			result += "];\n";
			return result;
		} catch (Exception e) {
			throw new BusinessException();
		}
	}

	@Override
	public Clusterizzation getSimilarityClusters(double threshold, ValuedRelationService valuedRelationService)
			throws BusinessException {
		List<Cluster> clusterList = new ArrayList<Cluster>();
		Clusterizzation clusterizzation = new Clusterizzation();
		List<ValuedRelation> valuedRelations = valuedRelationService.findAll(threshold);
		Map<String, Cluster> tempHash = new HashMap<String, Cluster>();
		for (ValuedRelation valuedRelation : valuedRelations) {
			String fromId = valuedRelation.getFromArtifact().getId();
			String toId = valuedRelation.getToArtifact().getId();
			if (!tempHash.containsKey(fromId) && !tempHash.containsKey(toId)) {
				Cluster c = new Cluster();
				c.getRelations().add(valuedRelation);
				c.getArtifacts().add(valuedRelation.getFromArtifact());
				c.getArtifacts().add(valuedRelation.getToArtifact());
				List<Property> propertyList = valuedRelation.getFromArtifact().getProperties();
				propertyList.addAll(valuedRelation.getToArtifact().getProperties());
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase().contains("domains"))
						c.getDomains().add(property.getValue());
				tempHash.put(fromId, c);
				tempHash.put(toId, c);
				clusterList.add(c);
			}
			if (tempHash.containsKey(fromId) && !tempHash.containsKey(toId)) {
				Cluster c = tempHash.get(fromId);
				c.getArtifacts().add(valuedRelation.getToArtifact());
				c.getRelations().add(valuedRelation);
				tempHash.put(toId, c);
				tempHash.put(fromId, c);
				List<Property> propertyList = valuedRelation.getToArtifact().getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase().contains("domains"))
						c.getDomains().add(property.getValue());
			}
			if (!tempHash.containsKey(fromId) && tempHash.containsKey(toId)) {
				Cluster c = tempHash.get(valuedRelation.getToArtifact().getId());
				c.getArtifacts().add(valuedRelation.getFromArtifact());
				c.getRelations().add(valuedRelation);
				tempHash.put(fromId, c);
				tempHash.put(toId, c);
				List<Property> propertyList = valuedRelation.getFromArtifact().getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase().contains("domains"))
						c.getDomains().add(property.getValue());
			}
			if (tempHash.containsKey(fromId) && tempHash.containsKey(toId)
					&& tempHash.get(fromId) != tempHash.get(toId)) {
				Cluster fromCluster = tempHash.get(fromId);
				Cluster toCluster = tempHash.get(toId);
				clusterList.remove(toCluster);
				clusterList.remove(fromCluster);
				fromCluster.getRelations().addAll(toCluster.getRelations());
				fromCluster.getArtifacts().addAll(toCluster.getArtifacts());
				fromCluster.getDomains().addAll(toCluster.getDomains());
				for (Artifact art : fromCluster.getArtifacts()) {
					Cluster cc = tempHash.get(art.getId());
					clusterList.remove(cc);
					tempHash.put(art.getId(), fromCluster);
				}
				clusterList.add(fromCluster);
				tempHash.put(toId, fromCluster);
				tempHash.put(fromId, fromCluster);
			}
		}

		List<EcoreMetamodel> ecoreMetamodels = findAll();
		for (EcoreMetamodel ecoreMetamodel : ecoreMetamodels) {
			if (tempHash.get(ecoreMetamodel.getId()) == null) {
				Cluster c = new Cluster();
				c.setMostRepresentive(ecoreMetamodel);
				c.getArtifacts().add(ecoreMetamodel);
				List<Property> propertyList = ecoreMetamodel.getProperties();
				for (Property property : propertyList)
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase().contains("domains"))
						c.getDomains().add(property.getValue());
				clusterList.add(c);
			}

		}

		for (Cluster cluster : clusterList) {
			int kMax = 0;
			int kMin = Integer.MAX_VALUE;
			int countRelation = 0;
			Artifact mostRepresentive = null;
			for (Artifact elem : cluster.getArtifacts()) {
				List<Relation> srl = findInCluster(elem, cluster);// similarityRelationService.findByEcoreMetamodel((EcoreMetamodel)elem,
																	// threshold,1);
				if (srl.size() > kMax) {
					kMax = srl.size();
					mostRepresentive = elem;
				}
				if (srl.size() < kMin)
					kMin = srl.size();
				countRelation += srl.size();
			}
			if (cluster.getArtifacts().size() == 1) {
				Iterator<Artifact> iterator = cluster.getArtifacts().iterator();
				mostRepresentive = iterator.next();
			} else

				cluster.setkMax(kMax);
			cluster.setkMin(kMin);
			cluster.setkAvg(countRelation / (cluster.getArtifacts().size() * 1.0));
			cluster.setMostRepresentive(mostRepresentive);
		}

		Collections.sort(clusterList, new Comparator<Cluster>() {
			@Override
			public int compare(Cluster cluster1, Cluster cluster2) {

				return cluster1.getArtifacts().size() > cluster2.getArtifacts().size() ? -1
						: cluster1.getArtifacts().size() < cluster2.getArtifacts().size() ? +1 : 0;
				// cluster1.getArtifacts().size().compareTo(cluster2.getArtifacts().size());
			}
		});

		clusterizzation.setClusters(clusterList);
		return clusterizzation;
	}

	@Override
	public List<Relation> findInCluster(Artifact elem, Cluster cluster) {
		List<Relation> result = new ArrayList<Relation>();
		for (Relation rel : cluster.getRelations()) {
			if (rel.getToArtifact().getId().equals(elem.getId()) || rel.getFromArtifact().getId().equals(elem.getId()))
				result.add(rel);
		}
		return result;
	}

	@Override
	public com.apporiented.algorithm.clustering.Cluster getHierarchicalCluster(
			ValuedRelationService valuedRelationService) throws BusinessException {
		String[] names = getNames();
		double[][] distances = getSimilarityMatrix(valuedRelationService);
		// try {
		// PrintWriter pw = new PrintWriter(new File (basePath + "j.txt"));
		// for (double[] ds : distances) {
		// for (double d : ds) {
		// pw.print(d + ";");
		// }
		// pw.println();
		// }
		// pw.close();
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// }
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		com.apporiented.algorithm.clustering.Cluster cluster = alg.performClustering(distances, names,
				new SingleLinkageStrategy());
		return cluster;
	}

	private String[] getNames() {
		List<EcoreMetamodel> emms = findAllPublic();
		String[] result = new String[emms.size()];
		for (int i = 0; i < emms.size(); i++)
			result[i] = emms.get(i).getName();
		return result;
	}

	private double[][] getSimilarityMatrix(ValuedRelationService valuedRelationService) {
		List<EcoreMetamodel> emms1 = findAllPublic();
		List<EcoreMetamodel> emms2 = emms1;
		List<ValuedRelation> sr = valuedRelationService.findAll();
		HashMap<String, ValuedRelation> map = new HashMap<String, ValuedRelation>();
		for (ValuedRelation similarityRelation : sr) {
			map.put(similarityRelation.getToArtifact().getId() + similarityRelation.getFromArtifact().getId(),
					similarityRelation);
			map.put(similarityRelation.getFromArtifact().getId() + similarityRelation.getToArtifact().getId(),
					similarityRelation);
		}
		double[][] result = new double[emms1.size()][emms2.size()];
		for (int i = 0; i < emms1.size(); i++) {
			for (int j = 0; j < emms2.size(); j++) {
				ValuedRelation srel = map.get(emms1.get(i).getId() + emms2.get(j).getId());
				if (srel != null)
					result[i][j] = 1 - srel.getValue();
				else {
					result[i][j] = 0;
				}
			}
		}
		return result;
	}

	@Override
	public double[][] getProximityMatrix(ValuedRelationService valuedRelationService) {
		List<EcoreMetamodel> emms1 = findAllPublic();
		List<EcoreMetamodel> emms2 = emms1;
		List<ValuedRelation> sr = valuedRelationService.findAll();
		HashMap<String, ValuedRelation> map = new HashMap<String, ValuedRelation>();
		for (ValuedRelation similarityRelation : sr) {
			map.put(similarityRelation.getToArtifact().getId() + similarityRelation.getFromArtifact().getId(),
					similarityRelation);
			map.put(similarityRelation.getFromArtifact().getId() + similarityRelation.getToArtifact().getId(),
					similarityRelation);
		}
		double[][] result = new double[emms1.size()][emms2.size()];
		for (int i = 0; i < emms1.size(); i++) {
			for (int j = 0; j < emms2.size(); j++) {
				ValuedRelation srel = map.get(emms1.get(i).getId() + emms2.get(j).getId());
				if (srel != null)
					result[i][j] = srel.getValue();
				else {
					result[i][j] = 1;
				}
			}
		}
		return result;
	}

	@Override
	public void printHierarchicalCluster(com.apporiented.algorithm.clustering.Cluster cluster,
			ValuedRelationService valuedRelationService) throws BusinessException {
		DendrogramPanel dp = new DendrogramPanel();
		dp.setModel(cluster);
		int w = 10000;
		int h = 10000;
		dp.setSize(w, h);
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		dp.paint(g);
		dp.print(g);
		File outputfile = new File("/hcluster.jpg");
		try {
			ImageIO.write(bi, "jpg", outputfile);
		} catch (IOException e) {
			throw new BusinessException();
		}

	}

	@Override
	public List<Cluster> getRealClustersFromHierarchicalCluster(
			List<com.apporiented.algorithm.clustering.Cluster> clusterList,
			ValuedRelationService valuedRelationService) {
		List<Cluster> result = new ArrayList<Cluster>();
		for (com.apporiented.algorithm.clustering.Cluster cluster : clusterList) {
			Cluster myCluster = new Cluster();
			List<com.apporiented.algorithm.clustering.Cluster> leaves = getClusterLeaf(cluster);
			for (com.apporiented.algorithm.clustering.Cluster leaf : leaves) {
				EcoreMetamodel emm = findOneByName(leaf.getName());
				myCluster.getArtifacts().add(emm);
				for (Property property : emm.getProperties())
					if (property.getName().toLowerCase().contains("domain")
							|| property.getName().toLowerCase().contains("domains"))
						myCluster.getDomains().add(property.getValue());
			}
			result.add(myCluster);
		}
		return result;
	}

	private List<com.apporiented.algorithm.clustering.Cluster> getClusterLeaf(
			com.apporiented.algorithm.clustering.Cluster cluster) {
		List<com.apporiented.algorithm.clustering.Cluster> result = new ArrayList<com.apporiented.algorithm.clustering.Cluster>();
		if (cluster.isLeaf())
			result.add(cluster);
		else
			for (com.apporiented.algorithm.clustering.Cluster c : cluster.getChildren()) {
				result.addAll(getClusterLeaf(c));
			}
		return result;
	}

	@Override
	public List<com.apporiented.algorithm.clustering.Cluster> getClustersWithThreshold(
			com.apporiented.algorithm.clustering.Cluster c, double threshold,
			ValuedRelationService valuedRelationService) throws BusinessException {
		List<com.apporiented.algorithm.clustering.Cluster> result = new ArrayList<com.apporiented.algorithm.clustering.Cluster>();
		if (c.getDistance() != null && c.getDistance() <= threshold)
			result.add(c);
		else if (c.isLeaf())
			result.add(c);
		else
			for (com.apporiented.algorithm.clustering.Cluster cluster : c.getChildren()) {
				result.addAll(getClustersWithThreshold(cluster, threshold, valuedRelationService));
			}
		return result;
	}

	// endregion

	@Override
	public List<EcoreMetamodel> searchByExample(EcoreMetamodel searchSample) throws BusinessException {
		Comparator<Double> c = new Comparator<Double>() {
			public int compare(Double a, Double b) {
				if (a >= b) {
					return -1;
				} else {
					return 1;
				}
			}
		};

		List<EcoreMetamodel> repository = findAll();
		Map<Double, EcoreMetamodel> list = new TreeMap<Double, EcoreMetamodel>(c);
		for (EcoreMetamodel ecoreMetamodel : repository) {
			double d = calculateContainment(ecoreMetamodel, searchSample);
			list.put(d, ecoreMetamodel);
		}
		logger.info(list.size() + "");
		List<EcoreMetamodel> result = new ArrayList<EcoreMetamodel>();
		int i = 0;
		for (Entry<Double, EcoreMetamodel> entry : list.entrySet()) {
			EcoreMetamodel value = entry.getValue();
			logger.info("score: " + entry.getKey());
			logger.info("metamodel" + entry.getValue().getName());
			result.add(value);
			try {
				// value.setScore(Float.parseFloat(entry.getKey().toString()));
			} catch (Exception e) {
				logger.error("fail to converter score to float");
			}
			if (i++ > 10)
				break;
		}
		return result;
	}

	@Override
	public List<EcoreMetamodel> searchByExample(EcoreMetamodel searchSample, double score) throws BusinessException {
		Comparator<Double> coparator = new Comparator<Double>() {
			public int compare(Double a, Double b) {
				if (a >= b) {
					return -1;
				} else {
					return 1;
				} // returning 0 would merge keys
			}
		};

		List<EcoreMetamodel> repository = findAll();
		Map<Double, EcoreMetamodel> list = new TreeMap<Double, EcoreMetamodel>(coparator);
		for (EcoreMetamodel ecoreMetamodel : repository) {
			double d = calculateContainment(ecoreMetamodel, searchSample);
			if (d >= score)
				list.put(d, ecoreMetamodel);
		}
		List<EcoreMetamodel> result = new ArrayList<EcoreMetamodel>();
		for (Entry<Double, EcoreMetamodel> entry : list.entrySet()) {
			EcoreMetamodel value = entry.getValue();
			result.add(value);
			try {
				value.setScore(Float.parseFloat(entry.getKey().toString()));
			} catch (Exception e) {
				logger.error("fail to converter score to float");
			}
		}
		return result;
	}

	@Override
	public double calculateContainment(EcoreMetamodel art1, EcoreMetamodel art2) {
		try {
			URI uri1 = URI.createFileURI(gridFileMediaService.getFilePath(art1));
			URI uri2 = URI.createFileURI(gridFileMediaService.getFilePath(art2));
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
			ResourceSet resourceSet1 = new ResourceSetImpl();
			ResourceSet resourceSet2 = new ResourceSetImpl();
			resourceSet1.getResource(uri1, true);
			resourceSet2.getResource(uri2, true);
			IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, null);

			// IMatchEngine.Factory.Registry me =
			// SemanticMatchEngineFactoryRegistryImpl.createStandaloneInstance();
			// Comparison comparison =
			// EMFCompare.builder().setMatchEngineFactoryRegistry(me).build().compare(scope);

			Comparison comparison = EMFCompare.builder().build().compare(scope);
			List<Match> matches = comparison.getMatches();
			int counter = 0;
			int counterLeft = 0;
			int counterRight = 0;
			for (Match match : matches) {
				List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
				for (Match match2 : lm) {
					if (match2.getLeft() != null)
						counterLeft++;
					if (match2.getRight() != null)
						counterRight++;
					if (match2.getLeft() != null && match2.getRight() != null)
						counter++;
				}

				if (match.getLeft() != null && match.getRight() != null)
					counter++;
			}
			double resultValue = (counter * 1.0) / ((counterLeft < counterRight) ? counterLeft : counterRight);
			// double resultValue = (counter * 1.0) / counterRight;
			return resultValue;
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public Cluster getCluster(EcoreMetamodel ecore, Clusterizzation clusterizzation) throws BusinessException {
		for (Cluster cluster : clusterizzation.getClusters()) {
			for (Artifact art : cluster.getArtifacts()) {
				if (art.equals(ecore))
					return cluster;
			}
		}
		throw new BusinessException();
	}

	@Override
	public Clusterizzation joinCluster(Clusterizzation c, Cluster from, Cluster to) {
		Clusterizzation result = new Clusterizzation();
		if (to.getArtifacts().size() == 1)
			result.setAlgoritmhs(c.getAlgoritmhs());
		result.setThreshold(c.getThreshold());
		for (Cluster cluster : c.getClusters()) {
			if (!cluster.getMostRepresentive().equals(from.getMostRepresentive())
					&& !cluster.getMostRepresentive().equals(to.getMostRepresentive())) {
				result.getClusters().add(cluster);
			}
			if (!cluster.getMostRepresentive().equals(from.getMostRepresentive())
					&& cluster.getMostRepresentive().equals(to.getMostRepresentive())) {
				cluster.getArtifacts().add(from.getMostRepresentive());
				result.getClusters().add(cluster);
			}
			if (cluster.getMostRepresentive().equals(from.getMostRepresentive())
					&& cluster.getMostRepresentive().equals(to.getMostRepresentive())) {
				result.getClusters().add(cluster);
			}
		}
		return result;

	}

	@Override
	public Clusterizzation recluster(Clusterizzation clusterizzation, double threshold,
			ValuedRelationService valuedRelationService) {

		Clusterizzation result = new Clusterizzation();
		boolean guard = false;
		for (Cluster cluster : clusterizzation.getClusters()) {
			if (cluster.getArtifacts().size() == 1) {
				EcoreMetamodel art = (EcoreMetamodel) cluster.getArtifacts().toArray()[0];
				Relation cont = valuedRelationService.findNearest(art, threshold);
				if (cont != null) {
					EcoreMetamodel to = (EcoreMetamodel) ((art.getId().equals(cont.getToArtifact().getId()))
							? cont.getFromArtifact() : cont.getToArtifact());
					if (!guard) {
						result = joinCluster(clusterizzation, cluster, getCluster(to, clusterizzation));
					} else {
						result = joinCluster(result, cluster, getCluster(to, result));
					}
					guard = true;
				}
			}
		}
		for (Cluster cluster : result.getClusters()) {
			cluster.setMostRepresentive(getMostRepresentativeElement(cluster, valuedRelationService));
			cluster.setDomains(getDescriptionFromCluster(cluster));
		}
		return result;
	}

	private Set<String> getDescriptionFromCluster(Cluster cluster) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJsonFormatFromResource(Resource metamodel) throws BusinessException {
		ObjectMapper mapper = new ObjectMapper();
		EMFModule emfModule = new EMFModule();
		mapper.registerModule(emfModule);

		try {
			
			String jsonString = mapper.writeValueAsString(metamodel);
			return jsonString;
		} catch (JsonProcessingException e1) {
			throw new BusinessException();
		}

	}

	@Override
	public String getJson(EcoreMetamodel id) throws BusinessException {
		if(!isValid(id))
				throw new InvalidArtifactException();
		Resource resource = this.registerMetamodel(id);
		return getJsonFormatFromResource(resource);
	}

	@Override
	public Artifact getMostRepresentativeElement(Cluster c, ValuedRelationService valuedRelationService)
			throws BusinessException {
		double max = 0;
		if (c.getArtifacts().size() == 1)
			return c.getArtifacts().iterator().next();
		Artifact result = null;
		for (Artifact art : c.getArtifacts()) {
			List<ValuedRelation> lr = valuedRelationService.findRelationsByArtifactInList(art, c.getArtifacts());
			double sum = 0;
			for (ValuedRelation relation : lr)
				sum += relation.getValue();
			if (sum > max) {
				max = sum;
				result = art;
			}
		}
		return result;
	}

	@Override
	public boolean checkConstraint(EPackage atlModel, List<OclExpression> expr) throws BusinessException {
		boolean result = true;
		for (OclExpression oclExpression : expr) {
			if (!checkConstraint(atlModel, oclExpression))
				result = false;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mdeforge.business.EcoreMetamodelService#checkConstraint(org.eclipse.
	 * emf.ecore.EPackage, anatlyzer.atlext.OCL.OclExpression)
	 */
	@Override
	public boolean checkConstraint(EPackage atlModel, OclExpression expr) throws BusinessException {
		try {
			// DEFINE OCL AND HELPER
			OCL<?, EClassifier, ?, ?, ?, EParameter, ?, ?, ?, Constraint, EClass, EObject> ocl;
			OCLHelper<EClassifier, ?, ?, Constraint> helper;

			// INSTANCIATE OCL
			ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
			// INSTANCIATE NEW HELPER FROM OCLEXPRESSION
			helper = ocl.createOCLHelper();
			// SET HELPER CONTEXT
			helper.setContext(ATLPackage.eINSTANCE.getModule());

			// CREATE OCLEXPRESSION
			OCLExpression<EClassifier> expression;
			expression = helper.createQuery("ecore::" + ATLSerializer.serialize(expr));

			// CREATE QUERY FROM OCLEXPRESSION
			Query<EClassifier, EClass, EObject> query = ocl.createQuery(expression);

			// EVALUATE OCL
			boolean success = query.check(atlModel);
			return success;
		} catch (ParserException e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Override
	public List<Statistic> numberOfMCdistribution() {
		MongoOperations n = new MongoTemplate(mongoDbFactory);
		List<Statistic> result = new ArrayList<Statistic>();
		Aggregation agg = newAggregation(match(Criteria.where("name").is("Number of concrete MetaClass")),
				project("value").andExpression("value").as("created"), group("value").count().as("total"),
				project("total").and("created").previousOperation(), sort(Sort.Direction.DESC, "created"));
		AggregationResults<Statistic> groupResults = n.aggregate(agg, SimpleMetric.class, Statistic.class);
		result = groupResults.getMappedResults();
		// Collections.sort(result, (Statistic p1, Statistic p2) ->
		// p1.firstName.compareTo(p2.firstName));
		List<Statistic> stat = new ArrayList<Statistic>(result);
		Collections.sort(stat, new Comparator<Statistic>() {
			public int compare(Statistic p1, Statistic p2) {
				int T1 = Integer.parseInt(p1.getCreated());
				int T2 = Integer.parseInt(p2.getCreated());
				int res = 0;
				res = (T1 > T2) ? 1 : -1;
				if (T1 == T2)
					res = 0;
				return res;
			}
		});

		return stat;

	}

	@Override
	public void calculateSimilarities(ToBeAnalyse toBeAnalyse) throws BusinessException {
		List<Artifact> tba = toBeAnalyseRepository.findAll().stream().map(z -> z.getArtifact())
				.collect(Collectors.toList());
		for (EcoreMetamodel ecoreMM : findAll()) {
			if (!tba.contains(ecoreMM))
				try {
					calculateSimilarity(toBeAnalyse.getArtifact(), ecoreMM);
				} catch (Exception e) {
					
				}
		}
	}

	@Override
	public List<String> getTagIndexes() {
		String[] metamodelLuceneTags = {LuceneServiceImpl.EPACKAGE_INDEX_CODE, LuceneServiceImpl.NsURI_INDEX_CODE, LuceneServiceImpl.EANNOTATION_INDEX_CODE,
				LuceneServiceImpl.ECLASS_INDEX_CODE, LuceneServiceImpl.EATTRIBUTE_INDEX_CODE,
				LuceneServiceImpl.EREFERENCE_INDEX_CODE, LuceneServiceImpl.EENUM_INDEX_CODE,
				LuceneServiceImpl.ELITERAL_INDEX_CODE, LuceneServiceImpl.EDATATYPE_INDEX_CODE};
		return Arrays.asList(metamodelLuceneTags);
	}

}