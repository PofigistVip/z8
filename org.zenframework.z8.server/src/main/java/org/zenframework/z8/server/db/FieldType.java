package org.zenframework.z8.server.db;

import java.sql.Types;

public enum FieldType {
	None(DataTypes.None),
	Guid(DataTypes.Guid),
	Boolean(DataTypes.Boolean),
	Integer(DataTypes.Integer),
	String(DataTypes.String),
	Date(DataTypes.Date),
	Datetime(DataTypes.DateTime),
	Datespan(DataTypes.DateSpan),
	Decimal(DataTypes.Decimal),
	Binary(DataTypes.Binary),
	Text(DataTypes.Text),
	File(DataTypes.File),
	Null(DataTypes.Null);

	class DataTypes {
		static protected final String None = "none";
		static protected final String Guid = "guid";
		static protected final String Boolean = "boolean";
		static protected final String Integer = "int";
		static protected final String String = "string";
		static protected final String Date = "date";
		static protected final String DateTime = "datetime";
		static protected final String DateSpan = "datespan";
		static protected final String Decimal = "float";
		static protected final String Binary = "binary";
		static protected final String Text = "text";
		static protected final String File = "file";
		static protected final String Null = "null";
	}

	private String fName = null;

	FieldType(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public FieldType fromString(String string) {
		if(DataTypes.None.equals(string))
			return FieldType.None;
		else if(DataTypes.Guid.equals(string))
			return FieldType.Guid;
		else if(DataTypes.Boolean.equals(string))
			return FieldType.Boolean;
		else if(DataTypes.Integer.equals(string))
			return FieldType.Integer;
		else if(DataTypes.String.equals(string))
			return FieldType.String;
		else if(DataTypes.Date.equals(string))
			return FieldType.Date;
		else if(DataTypes.DateTime.equals(string))
			return FieldType.Datetime;
		else if(DataTypes.DateSpan.equals(string))
			return FieldType.Datespan;
		else if(DataTypes.Decimal.equals(string))
			return FieldType.Decimal;
		else if(DataTypes.Binary.equals(string))
			return FieldType.Binary;
		else if(DataTypes.Text.equals(string))
			return FieldType.Text;
		else if(DataTypes.File.equals(string))
			return FieldType.File;
		else if(DataTypes.Null.equals(string))
			return FieldType.Null;
		else
			throw new RuntimeException("Unknown data type: '" + string + "'");
	}

	static public FieldType fromExcel(String type) {
		if(type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("TEXT"))
			return FieldType.String;
		else if(type.equalsIgnoreCase("NUMBER") || type.equalsIgnoreCase("CURRENCY"))
			return FieldType.Decimal;
		else if(type.equalsIgnoreCase("DATETIME"))
			return FieldType.Datetime;

		return FieldType.String;
	}

	public int jdbcType() {
		switch(this) {
		case Guid:
			return Types.CHAR;
		case Boolean:
			return Types.BIT;
		case Integer:
			return Types.BIGINT;
		case String:
			return Types.VARCHAR;
		case Date:
			return Types.DATE;
		case Datetime:
			return Types.TIMESTAMP;
		case Datespan:
			return Types.BIGINT;
		case Decimal:
			return Types.DECIMAL;
		case Text:
		case Binary:
			return Types.LONGVARBINARY;
		default:
			throw new RuntimeException("Unknown data type: '" + toString() + "'");
		}
	}

	public String vendorType(DatabaseVendor vendor) {
		switch(this) {
		case Binary:
		case Text:
			switch(vendor) {
			case Oracle: return "BLOB";
			case SqlServer: return "VARBINARY";
			case Postgres: return "bytea";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case Boolean:
			switch(vendor) {
			case Oracle: return "NUMBER";
			case SqlServer: return "TINYINT";
			case Postgres: return "smallint";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case Date:
		case Datetime:
			switch(vendor) {
			case Oracle: return "DATE";
			case SqlServer: return "DATETIME";
			case Postgres: return "timestamp with time zone";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case Datespan:
		case Integer:
			switch(vendor) {
			case Oracle: return "NUMBER";
			case SqlServer: return "BIGINT";
			case Postgres: return "bigint";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case Decimal:
			switch(vendor) {
			case Oracle: return "NUMBER";
			case SqlServer: return "NUMERIC";
			case Postgres: return "numeric";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case File:
			switch(vendor) {
			case Postgres: return "bytea";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case Guid:
			switch(vendor) {
			case Oracle: return "RAW";
			case SqlServer: return "UNIQUEIDENTIFIER";
			case Postgres: return "uuid";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		case String:
			switch(vendor) {
			case Oracle: return "NVARCHAR2";
			case SqlServer: return "NVARCHAR";
			case Postgres: return "character varying";
			default: throw new RuntimeException("Unknown data type: '" + toString() + "'");
			}
		default: 
			throw new RuntimeException("Unknown data type: '" + toString() + "'");
		}
	}
}
