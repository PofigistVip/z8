package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;

public class TruncHour extends SqlToken {
	private SqlToken time;

	public TruncHour(SqlToken time) {
		this.time = time;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		time.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			String dt = time.format(vendor, options);
			return "(" + dt + " - MOD(" + dt + ", 3600000))";
		case Postgres:
			return "date_trunc('hour', " + time.format(vendor, options) + ")";
		case SqlServer:
			return "Convert(datetime, convert(varchar(13)," + time.format(vendor, options) + ", 120) + ':00:00', 120)";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
