package de.avgl.dmp.persistence.service.impl;

import java.util.Map;
import java.util.Set;

import org.culturegraph.mf.types.Triple;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDBInputModel;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDbModel;
import de.avgl.dmp.persistence.service.InternalService;

@Singleton
public class InternalMemoryDbService extends BaseMemoryServiceImpl<Long, Long, Table<String, String, String>> implements InternalService {

	@Override
	public void createObject(final Long id, final Long id1, final Object model) throws DMPPersistenceException {

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		if (!MemoryDBInputModel.class.isInstance(model)) {

			throw new DMPPersistenceException("this service can only process memory DB input models");
		}

		final MemoryDBInputModel mdbim = (MemoryDBInputModel) model;

		synchronized (this) {
			final Optional<Table<String, String, String>> tableOptional = getObjects(id, id1);
			final Table<String, String, String> tab = tableOptional.or(HashBasedTable.<String, String, String> create());

			final Triple triple = mdbim.getTriple();

			tab.put(triple.getSubject(), triple.getPredicate(), triple.getObject());
			createObject(id, id1, tab);
		}
	}

	@Override
	public Optional<Map<String, Model>> getObjects(final Long id, final Long configurationId, final Optional<Integer> atMost)
			throws DMPPersistenceException {

		final Optional<Table<String, String, String>> maybeTable = getObjects(id, configurationId);

		if (maybeTable.isPresent()) {

			final Table<String, String, String> table = maybeTable.get();
			final Iterable<String> rows = atMost.isPresent() ? Iterables.limit(table.rowKeySet(), atMost.get()) : table.rowKeySet();

			final Map<String, Model> finalMap = Maps.newHashMap();

			for (final String row : rows) {
				final Map<String, String> recordMap = table.row(row);
				final MemoryDbModel model = new MemoryDbModel(recordMap);
				finalMap.put(row, model);
			}

			return Optional.fromNullable(finalMap);
		}

		return Optional.absent();
	}

	@Override
	public Optional<Set<String>> getSchema(final Long id, final Long configurationId) {
		synchronized (this) {
			final Optional<Table<String, String, String>> tab = getObjects(id, configurationId);
			return tab.transform(new Function<Table<String, String, String>, Set<String>>() {

				@Override
				public Set<String> apply(final Table<String, String, String> input) {
					return input.columnKeySet();
				}
			});
		}
	}
}