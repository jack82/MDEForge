<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


	<link href="${pageContext.request.contextPath}/resources/bootstrap/extend/jasny-fileupload/css/fileupload.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/resources/bootstrap/extend/bootstrap-wysihtml5/css/bootstrap-wysihtml5-0.0.2.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath}/resources/bootstrap/extend/bootstrap-select/bootstrap-select.css" rel="stylesheet" />
	<link href="${pageContext.request.contextPath}/resources/bootstrap/extend/bootstrap-toggle-buttons/static/stylesheets/bootstrap-toggle-buttons.css" rel="stylesheet" />



<!-- Breadcrumb START -->
<ul class="breadcrumb">
		<li>You are here</li>
		<li><a href="#" class="glyphicons dashboard"><i></i> Private Area</a></li>
		<li class="divider"></li>
		<li> Use</li>
		<li class="divider"></li>
		<li>Upload Page</li>
</ul>
<!-- Breadcrumb END -->



<h3>Upload Page</h3>


<div class="innerLR">

<form:form cssClass="form-horizontal" modelAttribute="artifact" 
	action="${pageContext.request.contextPath}/private/EcoreMetamodel/upload" role="form" method="POST" enctype="multipart/form-data">
	<div class="widget widget-heading-simple widget-body-gray">
		
			<!-- Widget heading -->
			<div class="widget-head">
				<h4 class="heading">Upload new Ecore Metamodel</h4>
			</div>
			<!-- // Widget heading END -->
			
			<div class="widget-body">
			
				<!-- Row -->
				<div class="row-fluid">
				
					<!-- Column -->
					<div class="span6">
					
						<!-- Group -->
						<div class="control-group">
							<label class="control-label" for="firstname">Metamodel Name</label>
							<div class="controls">
								<input type="text" name="name"></textarea>
							</div>
						</div>
						<div class="control-group">
							<label class="control-label" for="firstname">Metamodel Description</label>
							<div class="controls">
								<textarea class="span12" rows="5" cols="100" name="description"></textarea>
							</div>
						</div>
						<!-- // Group END -->
						
					</div>
					<!-- // Column END -->
					
					<!-- Column -->
					<div class="span6">
						<div class="row-fluid uniformjs">
				
							<!-- Column -->
							<div class="span4">
								<h4 style="margin-bottom: 10px;">Private or Public</h4>
								<div class="widget-body uniformjs collapse in">
								<div class="radio" id="uniform-undefined">
									<label class="radio">
										<span>
										<form:radiobutton cssClass="" path="open" value="true"/>Public
										</span>
									</label><br>
								<label class="radio">
									<form:radiobutton cssClass="" path="open" value="false"/>Private
								</label><br>
								</div>
								
							</div>
							</div>
							<!-- // Column END -->
						
							
						</div>
						<!-- // Row END -->
					</div>
					<!-- // Column END -->
				</div>
				<!-- // Row END -->
				<hr class="separator">
				<div class="row-fluid">
					<div class="control-group span6">
							<label class="control-label" for="email">Metamodel File</label>
							<div class="fileupload fileupload-new controls" data-provides="fileupload">
							  	<div class="input-append">
							    	<div class="uneditable-input"><i class="icon-file fileupload-exists"></i> 
							    		<span class="fileupload-preview"></span>
							    	</div>
							    	<span class="btn btn-default btn-file">
								    	<span class="fileupload-new">Select Metamodel File</span>
								    	<span class="fileupload-exists">Change</span>
								    	<input type="file" class="margin-none" name="metamodelfile" size="40"/>
							    	</span>
							    	<a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove</a>
							  	</div>
							</div>
						</div>
					</div>
					<div class="row-fluid">
				<!-- Row -->
					<div class="span6">
						<h4>Properties</h4>
							<table>
								<thead>
									<tr>
										<th>Name</th>
										<th>Value</th>
										<th>Action</th>
									</tr>
								</thead>
								<tbody id="propertiesTable">
								</tbody>
							</table>
							<button type="button" id="addPropertyButton" class="btn btn-icon btn-success glyphicons circle_ok"><i></i>Add new property</button>
					</div>
				</div>				
				<hr class="separator">
				<div class="row-fluid">
					<div class="span6">
					<h4>Add to project</h4>
						<form:select path="projects" multiple="true">
									<form:options  items="${projecList}" itemValue="id" itemLabel="name"></form:options>
						</form:select>
					</div>			
					
					<div class="span6">
					<h4>Share with:</h4>
						<form:select path="shared" multiple="true">
									<form:options  items="${userList}" itemValue="id" itemLabel="username"></form:options>
						</form:select>
					</div>				
				</div>
				<hr class="separator">
				
				<!-- Form actions -->
				<div class="row-fluid">
					
					<div class="form-actions">
						<button type="submit" class="btn btn-icon btn-primary glyphicons circle_ok"><i></i>Upload</button>
						<button type="button" class="btn btn-icon btn-default glyphicons circle_remove"><i></i>Cancel</button>
					</div>
				</div>
				<!-- // Form actions END -->
				
			</div>
		</div>
</form:form>



	
</div>	





<script src="${pageContext.request.contextPath}/resources/bootstrap/extend/jasny-fileupload/js/bootstrap-fileupload.js"></script>
<script src="${pageContext.request.contextPath}/resources/theme/scripts/plugins/forms/template/mustache.js"></script>
<script src="${pageContext.request.contextPath}/resources/theme/scripts/myscripts/dynamicRow.js"></script>
	