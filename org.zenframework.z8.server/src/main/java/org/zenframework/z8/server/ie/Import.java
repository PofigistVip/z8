package org.zenframework.z8.server.ie;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zenframework.z8.ie.xml.ExportEntry;
import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.generator.IForeignKey;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.request.Loader;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.primary;

public class Import {

	private static final Log LOG = LogFactory.getLog(Import.class);

	private static JsonObject STRUCTURE = null;

	public static void importRecords(Message message) {

		// Сортировка записей
		if (RecordsSorter.getSortingMode().onImport) {
			RecordsSorter sorter = new RecordsSorter();
			for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord()) {
				guid recordId = new guid(record.getRecordId());
				sorter.addRecord(record.getTable(), recordId);
				for (ExportEntry.Records.Record.Field field : record.getField()) {
					JsonObject tableInfo = getStructure().getJsonObject(record.getTable());
					if (tableInfo == null)
						throw new RuntimeException("No structure for '" + record.getTable() + "'");
					if (tableInfo.has(field.getId())) {
						String linkTableName = tableInfo.getString(field.getId());
						Table linkTable = (Table) Loader.getInstance(record.getTable());
						guid linkId = new guid(field.getValue());
						if (!IeUtil.isBuiltinRecord(linkTable, linkId)) {
							sorter.addLink(record.getTable(), recordId, linkTableName, linkId);
						}
					}
				}
			}
			Collections.sort(message.getExportEntry().getRecords().getRecord(), sorter.getComparator());
		}

		// Импорт записей
		for (ExportEntry.Records.Record record : message.getExportEntry().getRecords().getRecord())
			updateTableRecord((Table) Loader.getInstance(record.getTable()), record);

	}

	public static void importFiles(Message message, Files filesTable) {
		for (FileInfo file : message.getFiles()) {
			boolean idIsNull = file.id == null || file.id.isNull();
			if (idIsNull || !filesTable.hasRecord(file.id)) {
				if (!idIsNull) {
					filesTable.recordId.get().set(file.id);
				}
				filesTable.name.get().set(file.name);
				filesTable.file.get().set(file.getInputStream());
				filesTable.path.get().set(file.path);
				file.id = filesTable.create();
			}
		}
	}

	private static void updateTableRecord(Table table, ExportEntry.Records.Record record) {

		guid recordId = new guid(record.getRecordId());

		if (IeUtil.isBuiltinRecord(table, recordId))
			return;

		String policy = record.getPolicy();
		ImportPolicy recordPolicy = policy == null || policy.isEmpty() ? ImportPolicy.DEFAULT : ImportPolicy.valueOf(policy);

		Collection<Field> aggregatedFields = new LinkedList<Field>();
		for (ExportEntry.Records.Record.Field xmlField : record.getField()) {
			Field field = table.getFieldById(xmlField.getId());
			if (field != null) {
				policy = xmlField.getPolicy();
				if (policy == null || policy.isEmpty())
					policy = field.importPolicy();
				ImportPolicy fieldPolicy = policy == null || policy.isEmpty() ? recordPolicy : ImportPolicy.valueOf(policy);
				if (fieldPolicy == ImportPolicy.AGGREGATE)
					aggregatedFields.add(field);
			}
		}

		boolean exists = table.readRecord(recordId, aggregatedFields);

		boolean hasUpdatedFields = false;
		for (ExportEntry.Records.Record.Field xmlField : record.getField()) {
			Field field = table.getFieldById(xmlField.getId());
			if (field != null) {
				policy = xmlField.getPolicy();
				if (policy == null || policy.isEmpty())
					policy = field.importPolicy();
				ImportPolicy fieldPolicy = policy == null || policy.isEmpty() ? recordPolicy : ImportPolicy.valueOf(policy);
				if (!exists || fieldPolicy == ImportPolicy.OVERRIDE) {
					field.set(primary.create(field.type(), xmlField.getValue()));
					hasUpdatedFields = true;
				} else if (exists && fieldPolicy == ImportPolicy.AGGREGATE) {
					String aggregator = xmlField.getAggregator();
					if (aggregator == null || aggregator.isEmpty())
						aggregator = field.importAggregator();
					if (aggregator == null || aggregator.isEmpty())
						throw new RuntimeException("Can't aggregate " + field.id()
								+ " values. Import aggregator is not defined");
					try {
						ImportAggregator importAggregator = (ImportAggregator) Class.forName(aggregator).newInstance();
						field.set(importAggregator.aggregate(field.get(), primary.create(field.type(), xmlField.getValue())));
						hasUpdatedFields = true;
					} catch (Exception e) {
						throw new RuntimeException("Can't aggregate " + field.id() + " values", e);
					}
				}
			} else {
				Trace.logEvent("WARNING: Incorrect record format. Table '" + table.classId() + "' has no field '"
						+ xmlField.getId() + "'");
			}
		}

		if (!exists) {
			// Если запись не существует, создать
			LOG.debug("Import: create record " + IeUtil.toString(record));
			table.create(recordId);
		} else if (hasUpdatedFields) {
			// Если запись должна быть обновлена согласно политике, обновить
			LOG.debug("Import: update record " + IeUtil.toString(record));
			table.update(recordId);
		} else {
			// Если запись не должна быть обновлена, ничего не делать
			LOG.debug("Import: skip record " + IeUtil.toString(record));
		}

	}

	private static JsonObject getStructure() {
		if (STRUCTURE == null) {
			STRUCTURE = getTablesStructure();
		}
		return STRUCTURE;
	}

	public static JsonObject getTablesStructure() {
		JsonObject structure = new JsonObject();
		for (Table.CLASS<? extends Table> tableClass : Runtime.instance().tables()) {
			fillStructure(structure, tableClass);
		}
		return structure;
	}

	@SuppressWarnings("unchecked")
	private static void fillStructure(JsonObject structure, Table.CLASS<? extends Table> tableClass) {
		if (!structure.has(tableClass.classId())) {
			JsonObject jsonTable = new JsonObject();
			structure.put(tableClass.classId(), jsonTable);
			Table table = tableClass.newInstance();
			for (IForeignKey fkey : table.getForeignKeys()) {
				String fieldIndex = table.getFieldByName(fkey.getFieldDescriptor().name()).getIndex();
				jsonTable.put(fieldIndex, ((Table) fkey.getReferencedTable()).classId());
				fillStructure(structure,
						(Table.CLASS<? extends Table>) Loader.loadClass(((Table) fkey.getReferencedTable()).classId()));
			}
			Field parentKey = table.parentKey();
			if (parentKey != null) {
				jsonTable.put(parentKey.getIndex(), tableClass.classId());
			}
		}
	}

}
