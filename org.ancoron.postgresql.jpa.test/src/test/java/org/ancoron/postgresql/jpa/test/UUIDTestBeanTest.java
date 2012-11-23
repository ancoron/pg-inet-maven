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
package org.ancoron.postgresql.jpa.test;

import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ancoron
 */
public class UUIDTestBeanTest {

    // private static EJBContainer container;
    private static GlassFish glassfish;

    public UUIDTestBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        CommandRunner runner = glassfish.getCommandRunner();
        CommandResult res = runner.run("create-jdbc-connection-pool",
                "--datasourceclassname=org.postgresql.ds.PGSimpleDataSource",
                "--restype=javax.sql.DataSource",
                "--property=portNumber=5433:user=remote-tests:password=remote-tests:serverName=localhost:databaseName=pginettest",
                "test_pool");
        if(res.getExitStatus() == CommandResult.ExitStatus.FAILURE) {
            fail(res.getOutput());
        }
        res = runner.run("create-jdbc-resource",
                "--connectionpoolid=test_pool",
                "jdbc/pg_test");
        if(res.getExitStatus() == CommandResult.ExitStatus.FAILURE) {
            fail(res.getOutput());
        }
        glassfish.getDeployer().deploy(new File("target/classes"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // container.close();
        glassfish.dispose();
    }

    protected UUIDTestBeanLocal getBean() throws NamingException {
        return (UUIDTestBeanLocal) new InitialContext().lookup("java:global/UUIDTestBean");
    }
    
    @Before
    public void init() throws NamingException {
        List<UUID> ids = getBean().findAllIds();
        for(UUID id : ids) {
            getBean().deleteUUID(id);
        }
    }

    @Test
    public void testAddUUID() throws Exception {
        System.out.println("addUUID");
        
        String name = "my-name";
        UUIDTestBeanLocal instance = getBean();
        UUID result = instance.addUUID(name);
        assertNotNull("UUID has not been assigned", result);
    }

    @Test
    public void testFindByUUID() throws Exception {
        System.out.println("findByUUID");
        UUID uuid = getBean().addUUID("find-by-uuid");
        UUIDTestEntity expResult = new UUIDTestEntity();
        expResult.setId(uuid);
        expResult.setName("find-by-uuid");
        UUIDTestEntity result = getBean().findByUUID(uuid);
        assertEquals(expResult, result);
    }

    @Test
    public void testDeleteUUID() throws Exception {
        System.out.println("deleteUUID");
        UUID uuid = getBean().addUUID("delete-uuid");

        UUIDTestEntity expResult = new UUIDTestEntity();
        expResult.setId(uuid);
        expResult.setName("delete-uuid");
        UUIDTestEntity result = getBean().deleteUUID(uuid);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindByName() throws Exception {
        System.out.println("findByName");
        
        UUID uuid = getBean().addUUID("find-by-name");
        UUIDTestEntity expResult = getBean().findByUUID(uuid);

        UUIDTestEntity result = getBean().findByName(expResult.getName());
        assertEquals(expResult, result);
    }

    @Test
    public void testUpdate() throws Exception {
        System.out.println("update");
        
        UUID uuid = getBean().addUUID("update");
        getBean().update(uuid, "update-new-name");

        UUIDTestEntity expResult = new UUIDTestEntity();
        expResult.setId(uuid);
        expResult.setName("update-new-name");

        UUIDTestEntity result = getBean().findByName(expResult.getName());
        assertEquals(expResult, result);
    }
}
