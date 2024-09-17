# duckdb-kettle-plugin
A plugin for [Pentaho Data Integration (Kettle)](https://github.com/pentaho/pentaho-kettle) that adds support for [DuckDB](https://duckdb.org/) in the table input/output step.

## Background
This project was born out of experimentation with [Apache HOP](https://hop.apache.org/) and the realization that it supported DuckDB out of the box, while recent versions of Kettle did not :disappointed_relieved:

After a weekend of tinkering with the Kettle SDK and drawing inspiration from the DuckDB plugin in Apache HOP, this plugin brings the same functionality to Kettle.

## Install Instructions
Current and past releases can be found on the [Releases page](https://github.com/forgineer/duckdb-kettle-plugin/releases). Each release includes pre-compiled packages (zip files) containing the necessary JAR file(s) for installation, compiled for a specific version of PDI and DuckDB (see release notes). Download the most recent release that matches your version of Kettle.

> :memo: Releases may not always keep pace with the latest versions of DuckDB or [Pentaho Community Edition](https://support.pentaho.com/hc/en-us/articles/205789159-Pentaho-Product-Lifecycle-Overview). If you need a more recent version of DuckDB, see the [Build Instructions](#build-instructions) for compiling your own package.

1. Unpack the zip file into the `plugins` directory of your local Kettle install (`\data-integration\plugins`). 
2. The zip file should include the plugin and necessary JDBC driver for DuckDB, with the following strucutre:
```
data-integration\
    plugins\
        duckdb-kettle-plugin-x-x-x\
            lib\
                duckdb_jdbc-x.x.x.jar
            duckdb-kettle-plugin-x-x-x.jar
```
3. Restart Kettle (Spoon).

## Configure DuckDB Connection
After installing the plugin, DuckDB should be available as a connection type from a table input or output step, similar to a SQLite connection.

![DuckDB Connection](./images/duckdb-kettle-connection.png)

## Build Instructions

### Prerequisites
Before building the plugin, ensure you have the following installed and configured on your local machine:
* Maven, version 3+
* Java JDK 11 (or OpenJDK)
* The Pentaho Maven [settings.xml](https://raw.githubusercontent.com/pentaho/maven-parent-poms/master/maven-support-files/settings.xml) file in your home `.m2` directory

### Build the Plugin
1. Clone this repository locally:
```
git clone https://github.com/forgineer/duckdb-kettle-plugin.git
```
2. Review the `pom.xml` file and update the `<version>` tags and to match the same [Pentaho Data Integration (Kettle)](https://mvnrepository.com/artifact/pentaho-kettle/kettle-core) and [DuckDB](https://mvnrepository.com/artifact/org.duckdb/duckdb_jdbc) versions you intend to use. Verify each version on the [Maven repository](https://mvnrepository.com).
```xml
  <properties>
    <!-- Pentaho Data Integration (Kettle) Version -->
    <!-- Ex: 9.3.0.0-428, 9.4.0.0-343, etc. -->
    <kettle.version>10.0.0.0-221</kettle.version>
    <!-- DuckDB Version (JDBC driver) -->
    <!-- Ex: 0.10.2, 1.0.0, etc. -->
    <duckdb.version>1.0.0</duckdb.version>
    ...
  </properties>
```
3. Update the jar file name of the JDBC driver in the main source (`DuckDBDatabaseMeta.java`) to match the version found in the `pom.xml` file. Again, verify the file name and version on the [Maven repository](https://mvnrepository.com/artifact/org.duckdb/duckdb_jdbc) or directly from DuckDB.
```java
@Override
public String[] getUsedLibraries() {
    // The version should match POM
    return new String[] {"duckdb_jdbc-1.0.0.jar"};
}
```
4. Package the jar file:
```bash
mvn package 
```
This will create a jar and zip file inside of the `target` directory:
* duckdb-kettle-plugin-x.x.x.jar
* duckdb-kettle-plugin-x.x.x.zip

## Contributing
We welcome contributions to the duckdb-kettle-plugin project. Before submitting a pull request, please:
* Raise an [issue](https://github.com/forgineer/duckdb-kettle-plugin/issues) to discuss the proposed changes.
* Ensure that the issue is clear and concise, and that we've discussed and agreed on the changes.
This will help us to:
* Understand the purpose and scope of the changes
* Ensure that the changes align with the project's goals and architecture
* Provide guidance and support as needed