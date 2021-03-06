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
package org.dswarm.controller.eventbus;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Resource;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.ResourceStatics;
import org.dswarm.persistence.monitoring.MonitoringHelper;
import org.dswarm.persistence.monitoring.MonitoringLogger;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.util.SchemaDeterminator;

/**
 * An event recorder for converting XML or JSON documents.
 *
 * @author phorn
 * @author tgaengler
 */
public abstract class ConverterEventRecorder<CONVERTER_EVENT_IMPL extends ConverterEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(ConverterEventRecorder.class);

	/**
	 * The internal model service factory
	 */
	private final   InternalModelServiceFactory  internalServiceFactory;
	protected final Provider<MonitoringLogger>   loggerProvider;
	private final   Provider<SchemaDeterminator> schemaDeterminatorProvider;
	private final   String                       type;

	/**
	 * Creates a new event recorder for converting XML or JSON documents with the given internal model service factory and event bus.
	 *
	 * @param internalModelServiceFactory an internal model service factory
	 */
	public ConverterEventRecorder(
			final InternalModelServiceFactory internalModelServiceFactory,
			final Provider<MonitoringLogger> loggerProvider,
			final Provider<SchemaDeterminator> schemaDeterminatorProvider,
			final String typeArg) {

		internalServiceFactory = internalModelServiceFactory;
		this.loggerProvider = loggerProvider;
		this.schemaDeterminatorProvider = schemaDeterminatorProvider;
		type = typeArg;
	}

	/**
	 * Processes the XML or JSON document of the data model of the given event and persists the converted data.
	 *
	 * @param event an converter event that provides a data model
	 */
	// @Subscribe
	public void processDataModel(final CONVERTER_EVENT_IMPL event) throws DMPControllerException {

		final DataModel dataModel = event.getDataModel();
		final UpdateFormat updateFormat = event.getUpdateFormat();
		final boolean enableVersioning = event.isEnableVersioning();

		try (final MonitoringHelper ignore = loggerProvider.get().startIngest(dataModel)) {

			processDataModel(dataModel, updateFormat, enableVersioning);
		}
	}

	public void processDataModel(final DataModel dataModel, final UpdateFormat updateFormat, final boolean enableVersioning)
			throws DMPControllerException {

		final Observable<org.dswarm.persistence.model.internal.Model> result = doIngest(dataModel, false, Schedulers.newThread());

		try {

			final Observable<Response> writeResponse = internalServiceFactory.getInternalGDMGraphService()
					.updateObject(dataModel.getUuid(), result, updateFormat, enableVersioning);

			//LOG.debug("before to blocking");

			// TODO: delegate observable
			writeResponse.toBlocking().firstOrDefault(null);

			LOG.debug("processed {} data resource into data model '{}'", type, dataModel.getUuid());
		} catch (final DMPPersistenceException e) {

			final String message = String.format("couldn't persist the converted data of data model '%s'", dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}

	public Observable<org.dswarm.persistence.model.internal.Model> doIngest(final DataModel dataModel, final boolean utiliseExistingSchema,
			final Scheduler scheduler)
			throws DMPControllerException {

		// TODO: enable monitoring here

		LOG.debug("try to process {} data resource into data model '{}' (utilise existing schema = '{}')", type, dataModel.getUuid(),
				utiliseExistingSchema);

		try {

			final SchemaDeterminator schemaDeterminator = schemaDeterminatorProvider.get();
			final DataModel freshDataModel = schemaDeterminator.getSchemaInternal(dataModel.getUuid());
			final boolean isSchemaAnInbuiltSchema = schemaDeterminator.isSchemaAnInbuiltSchema(freshDataModel);
			final boolean hasSchema = isSchemaAnInbuiltSchema || utiliseExistingSchema;

			final AtomicInteger counter = new AtomicInteger(0);
			final AtomicLong statementCounter = new AtomicLong(0);

			//LOG.debug("XML records = '{}'", gdmModels.size());

			final String path = dataModel.getDataResource().getAttribute(ResourceStatics.PATH).asText();

			return convertData(dataModel, utiliseExistingSchema, scheduler, path, hasSchema).filter(gdmModel -> {

				try {

					final Model model = gdmModel.getModel();

					if (model == null) {

						LOG.debug("model is not available");

						return false;
					}

					final Collection<Resource> resources = model.getResources();

					if (resources == null || resources.isEmpty()) {

						LOG.debug("resources from model are not available");

						return false;
					}

					statementCounter.addAndGet(model.size());

					if (counter.incrementAndGet() == 1) {

						LOG.debug(
								"transformed first record of {} data resource at to GDM for data model '{}' with '{}' statements (data resource at '{}')",
								type, dataModel.getUuid(), statementCounter.get(), path);
					}

					schemaDeterminator.optionallyEnhancedDataModel(freshDataModel, gdmModel, model, hasSchema);

					//final int current = counter.incrementAndGet();

					//LOG.debug("XML resource number '{}' with '{}' and resources size = '{}'", current, resources.iterator().next().getUri(), resources.size());

					return true;
				} catch (DMPPersistenceException e) {

					final String message = String
							.format("something went wrong, while data model enhancement for data model '%s'", freshDataModel.getUuid());

					LOG.error(message, e);

					throw DMPPersistenceError.wrap(e);
				}
			}).cast(org.dswarm.persistence.model.internal.Model.class).doOnCompleted(
					() -> {

						final int recordCount = counter.get();

						if (recordCount == 0) {

							final Configuration configuration = dataModel.getConfiguration();
							final Optional<JsonNode> optionalRecordTagNode = Optional.ofNullable(
									configuration.getParameter(ConfigurationStatics.RECORD_TAG));

							final Optional<String> optionalRecordTag;

							if (optionalRecordTagNode.isPresent()) {

								optionalRecordTag = Optional.ofNullable(optionalRecordTagNode.get().asText(null));
							} else {

								optionalRecordTag = Optional.empty();
							}

							final String messageStart = String.format(
									"couldn't transform any record from %s data resource at '%s' to GDM for data model '%s'; ", type, path,
									dataModel.getUuid());

							final StringBuilder messageSB = new StringBuilder();
							messageSB.append(messageStart);

							if (optionalRecordTag.isPresent()) {

								messageSB.append("maybe you set a wrong record tag (current one = '").append(optionalRecordTag.get()).append("')");
							} else {

								messageSB.append("maybe because you set no record tag");
							}

							throw new RuntimeException(messageSB.toString());
						}

						LOG.debug(
								"transformed {} data resource at to GDM for data model '{}' - transformed '{}' records with '{}' statements (data resource at '{}')",
								type, dataModel.getUuid(), recordCount, statementCounter.get(), path);
					}).doOnSubscribe(
					() -> LOG.debug("subscribed to {} ingest", type));
		} catch (final NullPointerException e) {

			final String message = String.format("couldn't convert the %s data of data model '%s'", type, dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		} catch (final Exception e) {

			final String message = String.format("really couldn't convert the %s data of data model '%s'", type, dataModel.getUuid());

			ConverterEventRecorder.LOG.error(message, e);

			throw new DMPControllerException(String.format("%s %s", message, e.getMessage()), e);
		}
	}

	protected abstract Observable<GDMModel> convertData(final DataModel dataModel, final boolean utiliseExistingSchema, final Scheduler scheduler,
			final String path, final boolean hasSchema);
}
