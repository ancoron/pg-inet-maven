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
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;
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

            testConnection(conn);

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

    private void testConnection(Connection conn) throws SQLException {
        log.log(LogService.LOG_INFO, "Got JDBC connection: " + conn);

        assert(conn != null);

        String table = "tmp_tbl_" + UUID.randomUUID().toString().replaceAll("\\-", "");

        String tableDef = "(";
        tableDef += "c_id BIGINT PRIMARY KEY,";
        tableDef += "c_name CHARACTER VARYING,";
        tableDef += "c_net inet,";
        tableDef += "c_cidr cidr,";
        tableDef += "c_mac macaddr";
        tableDef += ")";

        execute(conn, "CREATE TABLE " + table + " " + tableDef);

        execute(conn, "SELECT * FROM " + table);
        
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + table
                + " (c_id, c_name, c_net, c_cidr, c_mac) VALUES (?, ?, ?, ?, ?)");

        stmt.setInt(1, 1);
        stmt.setString(2, "private LAN");
        stmt.setObject(3, new PGinet("1fe80::20e:cff:fe33:d204/64"));
        stmt.setObject(4, new PGcidr("192.168.167.0/24"));
        stmt.setObject(5, new PGmacaddr("00:0e:0c:33:d2:04"));
        assert(stmt.executeUpdate() == 1);
        stmt.close();
        
        stmt = conn.prepareStatement(" SELECT c_id, c_name, c_net, c_cidr, c_mac"
                + " FROM " + table);
        ResultSet rs = stmt.executeQuery();
        
        assert(rs.isBeforeFirst());
        
        while(rs.next()) {
            Object o = rs.getObject("c_net");
            assert (o != null);
            assert (o instanceof PGinet);
            PGinet inet = (PGinet) o;
            assert ("1fe80::20e:cff:fe33:d204/64".equals(inet.getValue()));

            o = rs.getObject("c_cidr");
            assert (o != null);
            assert (o instanceof PGcidr);
            PGcidr cidr = (PGcidr) o;
            assert ("192.168.167.0/24".equals(cidr.getValue()));

            o = rs.getObject("c_mac");
            assert (o != null);
            assert (o instanceof PGmacaddr);
            PGmacaddr mac = (PGmacaddr) o;
            assert ("00:0e:0c:33:d2:04".equals(mac.getValue()));
        }
        rs.close();
        stmt.close();
        

        execute(conn, "DROP TABLE " + table);

        conn.close();
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

        testConnection(conn);
        
        pc.close();
    }
}
