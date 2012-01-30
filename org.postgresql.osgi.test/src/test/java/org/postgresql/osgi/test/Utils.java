/*
 * Copyright 2011-2012 ancoron.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.postgresql.osgi.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 * Utility class for setting up things on a real PostgreSQL database.
 * 
 * <p>
 * To be able to use the tests you will have to make sure that we can connect
 * to some database and do the following commands:
 * <ul>
 * <li><tt>CREATE TABLE</tt></li>
 * <li><tt>DROP TABLE</tt></li>
 * <li><tt>SELECT</tt></li>
 * <li><tt>INSERT</tt></li>
 * </ul>
 * </p>
 * <p>
 * You have two options to accomplish this:
 * <ol>
 * <li>If you have some database that you want to allow for sharing you can set
 * the following system properties for the JVM during test execution (only 
 * specify the ones that differ from the defaults):
 * <table cellspacing="0" cellpadding="2" border="1">
 * <thead>
 * <tr><th>System property</th><th>Default value</th><th>Description</th></tr>
 * <tr><td><tt>pgsql.host</tt></td><td>localhost</td><td>The host name or IP address to connect to</td></tr>
 * <tr><td><tt>pgsql.port</tt></td><td>5432</td><td>The TCP port to connect to</td></tr>
 * <tr><td><tt>pgsql.database</tt></td><td>pg_inet_types_test</td><td>The name of the database for the tests</td></tr>
 * <tr><td><tt>pgsql.user</tt></td><td>pginettest</td><td>The name of the user used to establish connections</td></tr>
 * <tr><td><tt>pgsql.password</tt></td><td>pginettest</td><td>The password of the user used to establish connections</td></tr>
 * <tr><td><tt>pgsql.ssl</tt></td><td>false</td><td>Use SSL to connect?</td></tr>
 * </table>
 * </li>
 * <li>If you don't already have some database at hands you will need to 
 * create one.</li>
 * </ol>
 * </p>
 *
 * @author ancoron
 */
public class Utils {

    protected static final String PG_DATABASE;
    protected static final String PG_HOSTNAME;
    protected static final String PG_PASSWORD;
    protected static final String PG_PORT;
    protected static final boolean PG_SSL;
    protected static final String PG_USER;
    
    static {
        PG_HOSTNAME = System.getProperty("pgsql.host", "localhost");
        PG_PORT = "5432";
        PG_DATABASE = System.getProperty("pgsql.database", "pg_inet_types_test");
        PG_USER = System.getProperty("pgsql.user", "pginettest");
        PG_PASSWORD = System.getProperty("pgsql.password", "pginettest");
        PG_SSL = Boolean.getBoolean("pgsql.database");
    }

    public static String getPGDatabase() {
        return PG_DATABASE;
    }

    public static String getPGHostname() {
        return PG_HOSTNAME;
    }

    public static String getPGPassword() {
        return PG_PASSWORD;
    }

    public static String getPGPort() {
        return PG_PORT;
    }

    public static String getPGUser() {
        return PG_USER;
    }
    
    public static String getPGJDBCUrl() {
        return "jdbc:postgresql://" + PG_HOSTNAME + ":" + PG_PORT + "/" + PG_DATABASE;
    }

    /**
     * 
     * @return
     * @throws SQLException 
     */
    public static Connection openDB() throws SQLException {
        String url = getPGJDBCUrl();

        Properties props = new Properties();
        props.setProperty("user", PG_USER);
        props.setProperty("password", PG_PASSWORD);

        if(PG_SSL) {
            props.setProperty("ssl", "true");
        }
        
        Jdbc4Connection conn = (Jdbc4Connection) DriverManager.getConnection(url, props);
        
        conn.addDataType("cidr", PGcidr.class);
        conn.addDataType("inet", PGinet.class);
        conn.addDataType("macaddr", PGmacaddr.class);

        return conn;
    }

    public static void createTable(Connection dbConn, String tableName, String columnSpec) throws SQLException {
        Statement table = dbConn.createStatement();
        table.execute("CREATE TABLE " + tableName + " (" + columnSpec + ")");
        table.close();
    }

    public static void dropTable(Connection dbConn, String tableName) throws SQLException {
        Statement table = dbConn.createStatement();
        table.execute("DROP TABLE " + tableName);
        table.close();
    }

    public static String insertSQL(String tableName, String value) {
        return "INSERT INTO " + tableName + " VALUES (" + value + ")";
    }
}
