package org.zenframework.z8.server.db.sql.functions.date;

import java.util.Collection;

import org.zenframework.z8.server.base.table.value.IField;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.exceptions.db.UnknownDatabaseException;
import org.zenframework.z8.server.types.datespan;

public class AddSecond extends SqlToken {
	private SqlToken date;
	private SqlToken seconds;

	public AddSecond(SqlToken date, SqlToken seconds) {
		this.date = date;
		this.seconds = seconds;
	}

	@Override
	public void collectFields(Collection<IField> fields) {
		date.collectFields(fields);
		seconds.collectFields(fields);
	}

	@Override
	public String format(DatabaseVendor vendor, FormatOptions options, boolean logicalContext) {
		switch(vendor) {
		case Oracle:
			String dt = date.format(vendor, options);
			return "(" + dt + " + " + seconds.format(vendor, options) + " * " + datespan.TicksPerSecond + ")";
		case Postgres:
			return "(" + date.format(vendor, options) + " + (" + seconds.format(vendor, options) + ") * interval '1 second')";
		case SqlServer:
			return "DATEADD(ss, " + seconds.format(vendor, options) + ", " + date.format(vendor, options) + ")";
		default:
			throw new UnknownDatabaseException();
		}
	}

	@Override
	public FieldType type() {
		return FieldType.Date;
	}
}
