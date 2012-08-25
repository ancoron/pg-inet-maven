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
package org.ancoron.postgresql.jpa.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.test.mapping.NetworkEntity;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.postgresql.net.Driver;

/**
 *
 * @author ancoron
 */
public class DriverIntegrationTest {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(DriverIntegrationTest.class.getName());
    }

    private static EntityManagerFactory emFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            log.info("Building JPA EntityManager for unit tests");
            
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("javax.persistence.jdbc.url", TestUtil.getPGNetJDBCUrl());
            // properties.put("javax.persistence.jdbc.driver", Driver.class.getName());
            properties.put("javax.persistence.jdbc.user", TestUtil.getPGUser());
            properties.put("javax.persistence.jdbc.password", TestUtil.getPGPassword());
            
            emFactory = Persistence.createEntityManagerFactory("mapping-test-unit", properties);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Assert.fail("Exception during JPA EntityManager instanciation.");
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        log.info("Shuting down JPA layer.");
        if (emFactory != null) {
            emFactory.close();
        }
    }

    @Test
    public void testMapping() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();

            NetworkEntity net = new NetworkEntity(new IPNetwork("10.12.0.0/16"));
            
            em.persist(net);
            Assert.assertTrue(em.contains(net));

            em.getTransaction().commit();
            Assert.assertNotNull("NetworkEntity with PGinet "
                    + net.getNetwork().getValue() + " was not persisted",
                    net.getId());

            log.log(Level.INFO, "NetworkEntity with PGinet {0} has been persisted :)",
                    net.getNetwork().getValue());

            Long currentId = net.getId();
            
            // clear cache...
            em.detach(net);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(net));

            // testing find...
            em.getTransaction().begin();

            net = em.find(NetworkEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("NetworkEntity with ID " + currentId + " was not found", net);
            Assert.assertTrue(em.contains(net));

            log.log(Level.INFO, "NetworkEntity with ID {0} ({1}) has been found :)",
                    new Object[] {currentId, net.getNetwork().getValue()});
            
            // testing query...
            em.getTransaction().begin();

            String table = NetworkEntity.class.getAnnotation(Table.class).name();
            String column = NetworkEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();
            Query q = em.createNativeQuery("SELECT * FROM " + table + " b WHERE b." + column + " >>= #IPADDR",
                    NetworkEntity.class);
            q.setParameter("IPADDR", new IPNetwork("10.12.1.6"));
            List networks = q.getResultList();
            
            em.getTransaction().commit();
            Assert.assertEquals("Number of found NetworkEntities", 1, networks.size());
            
            net = (NetworkEntity) networks.get(0);
            Assert.assertTrue(em.contains(net));

            log.log(Level.INFO, "NetworkEntity with ID {0} ({1}) has been found :)",
                    new Object[] {currentId, net.getNetwork().getValue()});
        } catch (Exception ex) {
            if(em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
