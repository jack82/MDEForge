package org.mdeforge.presentation.backend;

import java.io.File;
import java.util.List;

import org.mdeforge.business.ContainmentRelationService;
import org.mdeforge.business.CosineSimilarityRelationService;
import org.mdeforge.business.DiceSimilarityRelationService;
import org.mdeforge.business.EcoreMetamodelService;
import org.mdeforge.business.GridFileMediaService;
import org.mdeforge.business.SimilarityRelationService;
import org.mdeforge.business.model.ContainmentRelation;
import org.mdeforge.business.model.CosineSimilarityRelation;
import org.mdeforge.business.model.DiceSimilarityRelation;
import org.mdeforge.business.model.EcoreMetamodel;
import org.mdeforge.business.model.SimilarityRelation;
import org.mdeforge.business.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/private/EcoreMetamodel")
public class EcoreMetamodelController {
	
	@Autowired
	private EcoreMetamodelService ecoreMetamodelService;
	@Autowired
	private GridFileMediaService gridFileMediaService;
	@Autowired
	private User user;

	@Autowired
	private SimilarityRelationService similarityRelationService;
	@Autowired
	private ContainmentRelationService containmentRelationService;
	@Autowired
	private DiceSimilarityRelationService diceSimilarityRelationService;
	@Autowired
	private CosineSimilarityRelationService cosineSimilarityRelationService;
	
	@RequestMapping(value = "/list", method=RequestMethod.GET, 
            produces= MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<EcoreMetamodel> getEcoreMetamodel () {
		List<EcoreMetamodel> list = ecoreMetamodelService.findAllWithPublicByUser(user);
		return list;
	}
	
	@RequestMapping(method = { RequestMethod.GET })
	public String ecoreMetamodel(Model model, @RequestParam String id) {
		
		EcoreMetamodel ecoreMM = ecoreMetamodelService.findOneById(id, user);		
		
		model.addAttribute("ecoreMM", ecoreMM);
		return "ecore_metamodel.details";
	}
	
	@RequestMapping(value = "/metamodel_details", method = { RequestMethod.GET })
	public String metamodelDetails(Model model, @RequestParam String metamodel_id) {

		EcoreMetamodel ecoreMetamodel = ecoreMetamodelService.findOnePublic(metamodel_id);
		model.addAttribute("ecoreMetamodel", ecoreMetamodel);
		String pathToDownload = gridFileMediaService.getFilePath(ecoreMetamodel);
		File ecoreMetamodelFile = new File(pathToDownload);
		model.addAttribute("ecoreMetamodelFile", ecoreMetamodelFile);

		return "private.use.metamodel_details";
	}

	@RequestMapping(value = "/metamodel_compare", method = { RequestMethod.GET })
	public String metamodelCompareStart(Model model) {
		
		List<EcoreMetamodel> ecoreMetamodelList = ecoreMetamodelService.findAllWithPublicByUser(user);
		model.addAttribute("ecoreMetamodelList", ecoreMetamodelList);
		
		return "private.use.metamodel_compare";
	}

	@RequestMapping(value = "/metamodel_compare", method = { RequestMethod.POST })
	public String metamodelCompareExecute(Model model, @RequestParam String left_metamodel_id, @RequestParam String right_metamodel_id) {
		
		EcoreMetamodel leftMetamodel = ecoreMetamodelService.findOne(left_metamodel_id);
		model.addAttribute("leftMetamodel", leftMetamodel);
		EcoreMetamodel rightMetamodel = ecoreMetamodelService.findOne(right_metamodel_id);
		model.addAttribute("rightMetamodel", rightMetamodel);
		
		/*
		 * Similarity Relations
		 */
		SimilarityRelation similarityRelation = similarityRelationService.findOneByArtifacts(leftMetamodel, rightMetamodel);
		model.addAttribute("similarityRelation", similarityRelation);
		CosineSimilarityRelation cosineSimilarityRelation = cosineSimilarityRelationService.findOneByArtifacts(leftMetamodel, rightMetamodel);
		model.addAttribute("cosineSimilarityRelation", cosineSimilarityRelation);
		DiceSimilarityRelation diceSimilarityRelation = diceSimilarityRelationService.findOneByArtifacts(leftMetamodel, rightMetamodel);
		model.addAttribute("diceSimilarityRelation", diceSimilarityRelation);
		ContainmentRelation containmentRelation = containmentRelationService.findOneByArtifacts(leftMetamodel, rightMetamodel);
		model.addAttribute("containmentRelation", containmentRelation);
		
		/*
		 * List of metamodels
		 */
		List<EcoreMetamodel> ecoreMetamodelList = ecoreMetamodelService.findAllWithPublicByUser(user);
		model.addAttribute("ecoreMetamodelList", ecoreMetamodelList);
		
		return "private.use.metamodel_compare";
	}
	
	

}
