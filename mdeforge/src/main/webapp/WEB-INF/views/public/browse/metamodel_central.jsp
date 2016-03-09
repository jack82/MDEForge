<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://www.springframework.org/security/tags"
	prefix="security"%>
<script
	src="${pageContext.request.contextPath}/resources/theme/scripts/wordcloud2.js"></script>

<div class="row-fluid">
	<div class="span12">
		<h4>Relation</h4>
		<div class="relativeWrap">
			<div class="widget widget-tabs">

				<!-- Tabs Heading -->
				<div class="widget-head">
					<ul>
						<li class="active"><a href="#mio_tab_1" data-toggle="tab"><i></i>Similarity</a></li>
						<li><a href="#mio_tab_2" data-toggle="tab"><i></i>Containment</a></li>
						<li><a href="#mio_tab_3" data-toggle="tab"><i></i>Cosine</a></li>
						<li><a href="#mio_tab_4" data-toggle="tab"><i></i>Dice</a></li>
						<li><a href="#mio_tab_5" data-toggle="tab"><i></i>Transformation
								From</a></li>
						<li><a href="#mio_tab_6" data-toggle="tab"><i></i>Transformation
								To</a></li>
					</ul>
				</div>
				<!-- // Tabs Heading END -->
				<div class="widget-body">
					<div class="tab-content">
						<!-- Tab content -->
						<div class="tab-pane active" id="mio_tab_1">
							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th>Artifact</th>
										<th class="center">Similarity Value</th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.SimilarityRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">

															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>

														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>


													<td class="center"><span class="badge badge-success"><fmt:formatNumber
																value="${relation.getValue()}" maxFractionDigits="3" /></span></td>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>


						</div>
						<!-- // Tab content END -->
						<!-- Tab content -->
						<div class="tab-pane" id="mio_tab_2">

							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th>Artifact</th>
										<th class="center">Containment Value</th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.ContainmentRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>
														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>


													<td class="center"><span class="badge badge-success"><fmt:formatNumber
																value="${relation.getValue()}" maxFractionDigits="3" /></span></td>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>

						</div>
						<!-- // Tab content END -->
						<div class="tab-pane" id="mio_tab_3">

							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th>Artifact</th>
										<th class="center">Containment Value</th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.CosineSimilarityRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>
														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>


													<td class="center"><span class="badge badge-success"><fmt:formatNumber
																value="${relation.getValue()}" maxFractionDigits="3" /></span></td>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>

						</div>
						<!-- // Tab content END -->
						<div class="tab-pane" id="mio_tab_4">

							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th>Artifact</th>
										<th class="center">Containment Value</th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.DiceSimilarityRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>
														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/metamodel_details?metamodel_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>


													<td class="center"><span class="badge badge-success"><fmt:formatNumber
																value="${relation.getValue()}" maxFractionDigits="3" /></span></td>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>

						</div>
						<!-- // Tab content END -->
						<!-- Tab content -->
						<div class="tab-pane" id="mio_tab_5">

							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th class="center"><spring:message
												code="mdeforge.relation.domainconformto" /></th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.DomainConformToRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/artifact?artifact_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>
														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/EcoreMetamodel/artifact?artifact_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>

						</div>
						<!-- // Tab content END -->
						<!-- Tab content -->
						<div class="tab-pane" id="mio_tab_6">

							<table class="table table-bordered table-striped table-white">

								<!-- Table heading -->
								<thead>
									<tr>
										<th class="center"><spring:message
												code="mdeforge.relation.codomainconformto"></spring:message></th>
									</tr>
								</thead>
								<!-- // Table heading END -->

								<!-- Table body -->
								<tbody>
									<c:forEach items="${artifact.relations}" var="relation">
										<!-- Table row -->
										<tr>
											<c:choose>
												<c:when
													test="${relation.getClass().name == 'org.mdeforge.business.model.CoDomainConformToRelation'}">
													<c:choose>
														<c:when
															test="${relation.getToArtifact().getId() == artifact.getId()}">
															<td><a
																href="${pageContext.request.contextPath}/public/ATLTransformation/artifact?artifact_id=${relation.getFromArtifact().getId()}">${relation.getFromArtifact().getName()}</a></td>
														</c:when>
														<c:otherwise>
															<td><a
																href="${pageContext.request.contextPath}/public/ATLTransformation/artifact?artifact_id=${relation.getToArtifact().getId()}">${relation.getToArtifact().getName()}</a></td>
														</c:otherwise>
													</c:choose>
												</c:when>
											</c:choose>
										</tr>
										<!-- // Table row END -->
									</c:forEach>
							</table>

						</div>
						<!-- // Tab content END -->
					</div>
				</div>
			</div>
		</div>
	</div>
</div>