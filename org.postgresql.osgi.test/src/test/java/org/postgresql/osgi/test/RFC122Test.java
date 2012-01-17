package org.postgresql.osgi.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import javax.inject.Inject;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import static org.ops4j.pax.exam.CoreOptions.*;

/*
 * Copyright 2012 ancoron.
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

/**
 *
 * @author ancoron
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class RFC122Test {
    
    @Inject
    private LogService log;

    @Inject
    private BundleContext ctx;
    
    @Configuration
    public Option[] configure() {
        return options(
                
                // this is how you set the default log level when using pax logging (logProfile)
                systemProperty( "org.ops4j.pax.logging.DefaultServiceLog.level" ).value( "INFO" ),
                
                mavenBundle("org.apache.felix", "org.apache.felix.log").versionAsInProject(),

                mavenBundle("org.osgi", "org.osgi.enterprise").versionAsInProject(),
                mavenBundle("org.ancoron.postgresql", "org.postgresql").versionAsInProject(),
                mavenBundle("org.ancoron.postgresql", "org.postgresql.net").versionAsInProject(),
                mavenBundle("org.ancoron.postgresql", "org.postgresql.osgi").versionAsInProject(),
                
                junitBundles(),
                
                felix()
        );
    }
    
    private DataSourceFactory getDSF() throws InvalidSyntaxException, InterruptedException {
        Filter filter = ctx.createFilter("(&(objectClass="
                + DataSourceFactory.class.getName() + ")("
                + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=org.postgresql.Driver))");
        
        ServiceTracker tracker = new ServiceTracker(ctx, filter, null);
        
        tracker.open();
        
        Object o = tracker.waitForService(10000);
        
        assert(o != null);
        
        return (DataSourceFactory) o;
    }
    
    private void execute(Connection conn, String sql) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.execute();
        
        ResultSet rs = ps.getResultSet();
        
        if(rs != null && rs.isBeforeFirst()) {
            log.log(LogService.LOG_INFO, "Result from '" + sql + "':" + rs);
        } else {
            log.log(LogService.LOG_INFO, "Successfully executed '" + sql + "'");
        }
        
        ps.close();
    }
    
    @Test
    public void createDataSource() {
        try {
            // positive test first...
            DataSourceFactory dsf = getDSF();

            Properties p = new Properties();
            p.setProperty(DataSourceFactory.JDBC_SERVER_NAME, Utils.getPGHostname());
            p.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, Utils.getPGPort());
            p.setProperty(DataSourceFactory.JDBC_USER, Utils.getPGUser());
            p.setProperty(DataSourceFactory.JDBC_PASSWORD, Utils.getPGPassword());
            p.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, Utils.getPGDatabase());

            DataSource ds = dsf.createDataSource(p);
            log.log(LogService.LOG_INFO, "Got JDBC data source: " + ds);

            assert(ds != null);

            Connection conn = ds.getConnection();
            log.log(LogService.LOG_INFO, "Got JDBC connection: " + conn);

            assert(conn != null);

            String table = "tmp_tbl_" + UUID.randomUUID().toString().replaceAll("\\-", "");

            String tableDef = "(";
            tableDef += "c_id BIGINT PRIMARY KEY,";
            tableDef += "c_name CHARACTER VARYING";
            tableDef += ")";

            execute(conn, "CREATE TABLE " + table + " " + tableDef);

            execute(conn, "SELECT * FROM " + table);

            execute(conn, "DROP TABLE " + table);

            conn.close();

            // now test negative...
            try {
                p.setProperty("invalid", "something");

                ds = dsf.createDataSource(p);

                Assert.fail("A data source has been created although we specified invalid parameters");
            } catch(SQLException x) {
                log.log(LogService.LOG_INFO, "Succeeded negative test: " + x.getMessage());
            }
        } catch(Exception x) {
            log.log(LogService.LOG_ERROR, x.getMessage(), x);
            throw new RuntimeException(x);
        }
    }
    
    @Test
    public void createConnectionPoolDataSource() throws InvalidSyntaxException, InterruptedException, SQLException {
        
        // positive test first...
        DataSourceFactory dsf = getDSF();
        
        Properties p = new Properties();
        p.setProperty(DataSourceFactory.JDBC_SERVER_NAME, Utils.getPGHostname());
        p.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, Utils.getPGPort());
        p.setProperty(DataSourceFactory.JDBC_USER, Utils.getPGUser());
        p.setProperty(DataSourceFactory.JDBC_PASSWORD, Utils.getPGPassword());
        p.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, Utils.getPGDatabase());

        ConnectionPoolDataSource ds = dsf.createConnectionPoolDataSource(p);
        
        assert(ds != null);
        
        PooledConnection pc = ds.getPooledConnection();
        
        assert(pc != null);
        
        Connection conn = pc.getConnection();
        
        assert(conn != null);
        
        String table = "tmp_tbl_" + UUID.randomUUID().toString().replaceAll("\\-", "");
        
        String tableDef = "(";
        tableDef += "c_id BIGINT PRIMARY KEY,";
        tableDef += "c_name CHARACTER VARYING";
        tableDef += ")";

        execute(conn, "CREATE TABLE " + table + " " + tableDef);

        execute(conn, "SELECT * FROM " + table);
        
        execute(conn, "DROP TABLE " + table);
        
        conn.close();
        
        pc.close();
    }
}
