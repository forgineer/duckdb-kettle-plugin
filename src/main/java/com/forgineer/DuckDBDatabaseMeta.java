package com.forgineer;

import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

@DatabaseMetaPlugin(
    type = "DuckDB",
    typeDescription = "DuckDB"
)

public class DuckDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
    @Override
    public int[] getAccessTypeList() {
        return new int[] {DatabaseMeta.TYPE_ACCESS_NATIVE};
    }

    /**
     * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
     */
    @Override
    public int getNotFoundTK(boolean useAutoinc) {
        if (supportsAutoInc() && useAutoinc) {
            return 1;
        }
        return super.getNotFoundTK(useAutoinc);
    }

    @Override
    public String getURL(String hostname, String port, String databaseName) {
        return "jdbc:duckdb:" + databaseName;
    }

    /**
     * @param tableName
     *      The table to be truncated.
     * @return
     *      The SQL statement to truncate a table: remove all rows from it without a transaction
     */
    @Override
    public String getTruncateTableStatement(String tableName) {
        return "DELETE FROM " + tableName;
    }

    @Override
    public String getDriverClass() {
        return "org.duckdb.DuckDBDriver";
    }

    /**
     * Generates the SQL statement to add a column to the specified table For this generic type, i set it to the most
     * common possibility.
     *
     * @param tablename
     *      The table to add
     * @param v
     *      The column defined as a value
     * @param tk
     *      The name of the technical key field
     * @param useAutoinc
     *      Whether or not this field uses auto increment
     * @param pk
     *      The name of the primary key field
     * @param semicolon
     *      Whether or not to add a semi-colon behind the statement.
     * @return 
     *      The SQL statement to add a column to the specified table
     */
    @Override
    public String getAddColumnStatement(String tableName, ValueMetaInterface  v, String tk, boolean useAutoinc , String pk, boolean semicolon) {
        return "ALTER TABLE "
                + tableName
                + " ADD COLUMN "
                + getFieldDefinition(v, tk, pk, useAutoinc, true, false);
    }

    /**
     * Generates the SQL statement to modify a column in the specified table
     *
     * @param tablename
     *      The table to add
     * @param v
     *      The column defined as a value
     * @param tk
     *      The name of the technical key field
     * @param useAutoinc
     *      Whether or not this field uses auto increment
     * @param pk
     *      The name of the primary key field
     * @param semicolon
     *      Whether or not to add a semi-colon behind the statement.
     * @return
     *      The SQL statement to modify a column in the specified table
     */
    @Override
    public String getModifyColumnStatement(String tableName, ValueMetaInterface  v, String tk, boolean useAutoinc, String pk, boolean semicolon) {
        return "ALTER TABLE "
                + tableName
                + " ALTER COLUMN "
                + getFieldDefinition(v, tk, pk, useAutoinc, true, false);
    }

    @Override
    public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc , boolean addFieldName, boolean add_cr) {
        // https://duckdb.org/docs/sql/data_types/overview.html
        String retval = "";

        String fieldname = v.getName();
        int length = v.getLength();
        int precision = v.getPrecision();

        if (addFieldName) {
            retval += fieldname + " ";
        }

        int type = v.getType();

        switch (type) {
            case ValueMetaInterface .TYPE_TIMESTAMP:
            case ValueMetaInterface .TYPE_DATE:
                retval += "TIMESTAMP";
                break;
            case ValueMetaInterface .TYPE_BOOLEAN:
                if (isSupportsBooleanDataType()) {
                    retval += "BOOLEAN";
                } else {
                    retval += "CHAR(1)";
                }
                break;
            case ValueMetaInterface .TYPE_NUMBER:
            case ValueMetaInterface .TYPE_INTEGER:
            case ValueMetaInterface .TYPE_BIGNUMBER:
                if (fieldname.equalsIgnoreCase(tk) ||   // Technical key
                    fieldname.equalsIgnoreCase(pk)      // Primary key
                    ) {
                    retval += "IDENTITY";
                } else {
                    if (length > 0) {
                        if (precision > 0 || length > 18) {
                            retval += "DECIMAL(" + length + ", " + precision + ")";
                        } else {
                            if (length > 9) {
                                retval += "BIGINT";
                            } else {
                                if (length < 5) {
                                    if (length < 3) {
                                        retval += "TINYINT";
                                    } else {
                                        retval += "SMALLINT";
                                    }
                                } else {
                                    retval += "INTEGER";
                                }
                            }
                        }

                    } else {
                        retval += "DOUBLE";
                    }
                }
                break;
            case ValueMetaInterface .TYPE_STRING:
                if (length >= DatabaseMeta.CLOB_LENGTH) {
                    retval += "TEXT";
                } else {
                    retval += "VARCHAR";
                    if (length > 0) {
                        retval += "(" + length;
                    } else {
                        retval += "(" + Integer.MAX_VALUE;
                    }
                    retval += ")";
                }
                break;
            case ValueMetaInterface .TYPE_BINARY:
                retval += "BLOB";
                break;
            default:
                retval += "UNKNOWN";
                break;
        }

        if (add_cr) {
            retval += Const.CR;
        }

        return retval;
    }

    @Override
    public String[] getUsedLibraries() {
        // The version should match POM
        return new String[] {"duckdb_jdbc-1.1.0.jar"};
    }

    /**
     * Returns reserved words for DuckDB
     */
    @Override
    public String[] getReservedWords() {
        return new String[] {"ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC_P", "ASYMMETRIC", "BOTH", "CASE", "CAST",
                            "CHECK_P", "COLLATE", "COLUMN", "CONSTRAINT", "CREATE_P", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_ROLE", 
                            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC_P", "DISTINCT", "DO", 
                            "ELSE", "END_P", "EXCEPT", "FALSE_P", "FETCH", "FOR", "FOREIGN", "FROM", "GRANT", "GROUP_P", "HAVING", "IN_P", 
                            "INITIALLY", "INTERSECT", "INTO", "LATERAL_P", "LEADING", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NOT", "NULL_P", 
                            "OFFSET", "ON", "ONLY", "OR", "ORDER", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "SELECT", "SESSION_USER", 
                            "SOME", "SYMMETRIC", "TABLE", "THEN", "TO", "TRAILING", "TRUE_P", "UNION", "UNIQUE", "USER", "USING", "VARIADIC", 
                            "WHEN", "WHERE", "WINDOW", "WITH"};
    }

    public boolean isSupportsBooleanDataType() {
        return true;
    }

    public boolean isSupportsTimestampDataType() {
        return true;
    }
}