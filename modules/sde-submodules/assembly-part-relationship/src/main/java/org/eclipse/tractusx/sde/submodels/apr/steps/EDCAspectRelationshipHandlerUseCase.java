/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022, 2023 T-Systems International GmbH
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.sde.submodels.apr.steps;

import static org.eclipse.tractusx.sde.common.constants.CommonConstants.ASSET_PROP_ID;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.constants.CommonConstants;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.exception.ServiceException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.service.AspectRelationshipService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EDCAspectRelationshipHandlerUseCase extends Step {

	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final PolicyRequestFactory policyFactory;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;
	private final AspectRelationshipService aspectRelationshipService;

	public EDCAspectRelationshipHandlerUseCase(EDCGateway edcGateway, AssetEntryRequestFactory assetFactory,
			PolicyRequestFactory policyFactory, ContractDefinitionRequestFactory contractFactory,
			PolicyConstraintBuilderService policyConstraintBuilderService,
			AspectRelationshipService aspectRelationshipService) {
		this.assetFactory = assetFactory;
		this.edcGateway = edcGateway;
		this.policyFactory = policyFactory;
		this.contractFactory = contractFactory;
		this.policyConstraintBuilderService = policyConstraintBuilderService;
		this.aspectRelationshipService = aspectRelationshipService;
	}

	@SneakyThrows
	public AspectRelationship run(String submodel, AspectRelationship input, String processId) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel,
					getSubmodelShortDescriptionOfModel(), shellId, subModelId, input.getParentUuid());
			if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get(ASSET_PROP_ID))) {

				if (CommonConstants.UPDATED_Y.equals(input.getUpdated())
						&& input.getOldSubmodelIdforUpdateCase() != null) {
					deleteIfAnyReferenceExist(input.getOldSubmodelIdforUpdateCase());
				}

				edcProcessingforAspectRelationship(assetEntryRequest, input);

			} else {

				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforAspectRelationship(assetEntryRequest, input);
				input.setUpdated(CommonConstants.UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}

	private void deleteIfAnyReferenceExist(String oldSubModelId) {
		try {
			AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipService
					.readEntityBySubModelId(oldSubModelId);
			aspectRelationshipService.deleteEDCAsset(aspectRelationshipEntity);
			log.info(
					"Deleted existing EDC asset in update case reference,so EDC end will be single asset for each submodel: "
							+ oldSubModelId);
		} catch (Exception e) {
			log.warn(
					"Trying to delete existing EDC asset in update case reference,so EDC end will be single asset for each submodel: "
							+ e.getMessage());
		}

	}

	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, AspectRelationship input, String processId) {
		try {
			AspectRelationshipEntity aspectRelationshipEntity = aspectRelationshipService
					.readEntity(input.getChildUuid());
			aspectRelationshipService.deleteEDCAsset(aspectRelationshipEntity);
		} catch (Exception e) {
			if (!e.getMessage().contains("404 Not Found")) {
				throw new ServiceException("Exception in EDC delete request process: " + e.getMessage());
			}
		}
	}

	@SneakyThrows
	private void edcProcessingforAspectRelationship(AssetEntryRequest assetEntryRequest, AspectRelationship input) {
		HashMap<String, String> extensibleProperties = new HashMap<>();

		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();
		edcGateway.createAsset(assetEntryRequest);

		List<ConstraintRequest> usageConstraints = policyConstraintBuilderService
				.getUsagePolicyConstraints(input.getUsagePolicies());
		List<ConstraintRequest> accessConstraints = policyConstraintBuilderService
				.getAccessConstraints(input.getBpnNumbers());

		String customValue = getCustomValue(input);
		if (StringUtils.isNotBlank(customValue)) {
			extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
		}

		PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				accessConstraints, new HashMap<>());
		PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				usageConstraints, extensibleProperties);

		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
				assetEntryRequest.getAsset().getProperties().get(ASSET_PROP_ID), accessPolicyDefinitionRequest.getId(),
				usagePolicyDefinitionRequest.getId());

		edcGateway.createContractDefinition(contractDefinitionRequest);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getProperties().get(ASSET_PROP_ID));
		input.setAccessPolicyId(accessPolicyDefinitionRequest.getId());
		input.setUsagePolicyId(usagePolicyDefinitionRequest.getId());
		input.setContractDefinationId(contractDefinitionRequest.getId());
	}

	private String getCustomValue(AspectRelationship input) {
		if (!CollectionUtils.isEmpty(input.getUsagePolicies())) {
			return input.getUsagePolicies().stream().filter(policy -> policy.getType().equals(UsagePolicyEnum.CUSTOM))
					.map(value -> value.getValue()).findFirst().orElse(null);
		}
		return null;
	}
}
