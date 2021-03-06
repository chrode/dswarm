/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.service.internal.graph.util;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * @author tgaengler
 */
public class SchemaDeterminator {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaDeterminator.class);

	/**
	 * The schema persistence service.
	 */
	private final Provider<SchemaService> schemaService;

	/**
	 * The class persistence service.
	 */
	private final Provider<ClaszService> classService;

	private final Provider<AttributePathService> attributePathService;

	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceService;

	private final Provider<AttributeService> attributeService;

	/**
	 * The data model persistence service.
	 */
	private final Provider<DataModelService> dataModelService;

	/**
	 * Creates a new internal triple service with the given persistence services and the endpoint to access the graph database.
	 *
	 * @param dataModelService     the data model persistence service
	 * @param schemaService        the schema persistence service
	 * @param classService         the class persistence service
	 * @param attributePathService the attribute path persistence service
	 * @param attributeService     the attribute persistence service
	 */
	@Inject
	public SchemaDeterminator(
			final Provider<DataModelService> dataModelService,
			final Provider<SchemaService> schemaService,
			final Provider<ClaszService> classService,
			final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceService,
			final Provider<AttributePathService> attributePathService,
			final Provider<AttributeService> attributeService) {

		this.dataModelService = dataModelService;
		this.schemaService = schemaService;
		this.classService = classService;
		this.attributePathService = attributePathService;
		this.schemaAttributePathInstanceService = schemaAttributePathInstanceService;
		this.attributeService = attributeService;
	}

	public DataModel optionallyEnhancedDataModel(final DataModel dataModel, final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel,
			final boolean utiliseExistingSchema)
			throws DMPPersistenceException {

		if (!utiliseExistingSchema) {

			return determineSchema(dataModel, gdmModel, realModel);
		} else {

			return dataModel;
		}
	}

	public boolean isSchemaAnInbuiltSchema(final DataModel dataModel) throws DMPPersistenceException {

		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			final String schemaUUID = schema.getUuid();

			if (schemaUUID != null) {

				switch (schemaUUID) {

					case SchemaUtils.MABXML_SCHEMA_UUID:
					case SchemaUtils.MARC21_SCHEMA_UUID:
					case SchemaUtils.PNX_SCHEMA_UUID:
					case SchemaUtils.FINC_SOLR_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID:
					case SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID:

						// those schemas are already there and shouldn't be manipulated by data that differs from those schemas
						LOG.debug("schema for data model '{}' is a preset schema, so everything is already set", dataModel.getUuid());

						return true;
				}
			}
		}

		return false;
	}

	public DataModel getSchemaInternal(final String dataModelUuid) throws DMPPersistenceException {

		final DataModel dataModel = getDataModel(dataModelUuid);

		final Schema schema;

		if (dataModel != null && dataModel.getSchema() == null) {

			final Configuration configuration = dataModel.getConfiguration();

			Optional<String> optionalPresetSchema = null;

			if (configuration != null) {

				final JsonNode storageTypeJsonNode = configuration.getParameter(ConfigurationStatics.STORAGE_TYPE);

				if (storageTypeJsonNode != null) {

					final String storageType = storageTypeJsonNode.asText();

					if (storageType != null) {

						switch (storageType) {

							case ConfigurationStatics.MABXML_STORAGE_TYPE:
							case ConfigurationStatics.MARCXML_STORAGE_TYPE:
							case ConfigurationStatics.PNX_STORAGE_TYPE:
							case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
							case ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE:
							case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
							case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

								optionalPresetSchema = Optional.of(storageType);

								LOG.debug("found storage type '{}' for preset schema", storageType);

								break;
						}

					}
				}
			}

			if (optionalPresetSchema == null || !optionalPresetSchema.isPresent()) {

				// create new schema
				final ProxySchema proxySchema = schemaService.get().createObjectTransactional();

				if (proxySchema != null) {

					schema = proxySchema.getObject();
				} else {

					schema = null;
				}
			} else {

				switch (optionalPresetSchema.get()) {

					case ConfigurationStatics.MABXML_STORAGE_TYPE:

						// assign existing mabxml schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.MABXML_SCHEMA_UUID);

						break;
					case ConfigurationStatics.MARCXML_STORAGE_TYPE:

						// assign existing marc21 schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.MARC21_SCHEMA_UUID);

						break;
					case ConfigurationStatics.PNX_STORAGE_TYPE:

						// assign existing pnx schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.PNX_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:

						// assign existing OAI-PMH + DC Elements schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAI_PMH_DCE_AND_EDM_ELEMENTS_STORAGE_TYPE:

						// assign existing OAI-PMH + DC Elements + EDM schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:

						// assign existing OAI-PMH + DC Terms schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID);

						break;
					case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

						// assign existing OAI-PMH + MARCXML schema to data resource

						schema = schemaService.get().getObject(SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID);

						break;
					default:

						LOG.debug("could not determine and set preset schema for identifier '{}'", optionalPresetSchema.get());

						schema = null;
				}
			}

			LOG.debug("set preset schema for data model '{}'", dataModel.getUuid());

			dataModel.setSchema(schema);
		}

		return updateDataModel(dataModel);
	}

	private DataModel determineSchema(final DataModel dataModel, final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel)
			throws DMPPersistenceException {

		LOG.debug("determine schema for data model '{}'", dataModel.getUuid());

		final DataModel updatedDataModel = addRecordClass(dataModel, gdmModel.getRecordClassURI());

		if (updatedDataModel == null) {

			throw new DMPPersistenceException("Could not get the actual data model to use");
		}

		determineRecordResources(gdmModel, realModel, updatedDataModel);

		determineAttributePaths(updatedDataModel, gdmModel);

		LOG.debug("determined schema for data model '{}'", dataModel.getUuid());

		return updatedDataModel;
	}

	private void determineRecordResources(final GDMModel gdmModel, final org.dswarm.graph.json.Model realModel, final DataModel finalDataModel) {

		LOG.debug("determine record resources for data model '{}'", finalDataModel.getUuid());

		if (finalDataModel.getSchema() != null) {

			if (finalDataModel.getSchema().getRecordClass() != null) {

				gdmModel.setRecordURIs(realModel.getResourceURIs());
			}
		}

		LOG.debug("determined record resources for data model '{}'", finalDataModel.getUuid());
	}

	/**
	 * Adds the record class to the schema of the data model.
	 *
	 * @param dataModel the data model
	 * @param recordClassUri the identifier of the record class
	 * @throws DMPPersistenceException
	 */
	private DataModel addRecordClass(final DataModel dataModel, final String recordClassUri) throws DMPPersistenceException {

		LOG.debug("add record class '{}' to schema for data model '{}'", recordClassUri, dataModel.getUuid());

		// (try) add record class uri to schema
		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			final boolean result = SchemaUtils.addRecordClass(schema, recordClassUri, classService);

			if (!result) {

				return dataModel;
			}

			LOG.debug("added record class to schema for data model '{}'", dataModel.getUuid());

			return updateDataModel(dataModel);
		}

		return dataModel;
	}

	private DataModel determineAttributePaths(final DataModel dataModel, final Model model)
			throws DMPPersistenceException {

		LOG.debug("determine attribute paths of schema for data model '{}'", dataModel.getUuid());

		final Schema schema = dataModel.getSchema();

		if (schema != null) {

			// note: model.getAttributePaths is expensive atm
			final Set<AttributePathHelper> attributePathHelpers = model.getAttributePaths();

			final boolean result = SchemaUtils.addAttributePaths(schema, attributePathHelpers,
					attributePathService, schemaAttributePathInstanceService, attributeService);

			if (!result) {

				return dataModel;
			}

			LOG.debug("determined attribute paths of schema for data model '{}'", dataModel.getUuid());

			return updateDataModel(dataModel);
		}

		return dataModel;
	}

	private DataModel updateDataModel(final DataModel dataModel) throws DMPPersistenceException {
		final ProxyDataModel proxyUpdatedDataModel = dataModelService.get().updateObjectTransactional(dataModel);

		if (proxyUpdatedDataModel == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedDataModel.getObject();
	}

	private DataModel getDataModel(final String dataModelUuid) {

		final DataModel dataModel = dataModelService.get().getObject(dataModelUuid);

		if (dataModel == null) {

			LOG.error("couldn't find data model '{}'", dataModelUuid);

			return null;
		}

		return dataModel;
	}
}
