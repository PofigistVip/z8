package org.zenframework.z8.server.db.sql;

import java.util.Collection;
import java.util.HashSet;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;

abstract public class SqlToken {
	private Collection<IField> usedFields = null;

	public String format(DatabaseVendor vendor, FormatOptions options) {
		return format(vendor, options, false);
	}

	abstract public void collectFields(Collection<IField> fields);

	abstract public String format(DatabaseVendor vendor, FormatOptions options, boolean isLogicalContext);

	public Collection<IField> getUsedFields() {
		if(usedFields == null) {
			usedFields = new HashSet<IField>();
			collectFields(usedFields);
		}
		return usedFields;
	}

	public boolean isNumeric() {
		return isInteger() || isDecimal();
	}

	public boolean isInteger() {
		return type() == FieldType.Integer;
	}

	public boolean isDecimal() {
		return type() == FieldType.Decimal;
	}

	public boolean isDate() {
		return type() == FieldType.Date || type() == FieldType.Datetime;
	}

	public boolean isDatespan() {
		return type() == FieldType.Datespan;
	}

	abstract public FieldType type();
}
