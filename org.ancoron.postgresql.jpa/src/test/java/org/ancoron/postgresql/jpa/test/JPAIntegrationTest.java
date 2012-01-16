/*
 * Copyright 2011 ancoron.
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

import java.sql.Driver;
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
import javax.persistence.TypedQuery;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.junit.Test;
import org.postgresql.util.PGobject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.postgresql.net.PGinet;

/**
 *
 * @author ancoron
 */
public class JPAIntegrationTest {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(JPAIntegrationTest.class.getName());
    }

    private static EntityManagerFactory emFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            log.info("Building JPA EntityManager for unit tests");
            
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("javax.persistence.jdbc.url", TestUtil.getPGJDBCUrl());
            properties.put("javax.persistence.jdbc.driver", Driver.class.getName());
            properties.put("javax.persistence.jdbc.user", TestUtil.getPGUser());
            properties.put("javax.persistence.jdbc.password", TestUtil.getPGPassword());
            
            emFactory = Persistence.createEntityManagerFactory("test-unit", properties);
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
    public void testPGinet() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();

            PGinetEntity net = new PGinetEntity("10.10.0.0/16");
            
            em.persist(net);
            Assert.assertTrue(em.contains(net));

            em.getTransaction().commit();
            Assert.assertNotNull("PGinetEntity with PGinet " + net.getNetwork().getValue() + " was not persisted", net.getId());

            log.log(Level.INFO, "PGinetEntity with PGinet {0} has been persisted :)",
                    net.getNetwork().getValue());

            Long currentId = net.getId();
            
            // clear cache...
            em.detach(net);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(net));

            // testing find...
            em.getTransaction().begin();

            net = em.find(PGinetEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("PGinetEntity with ID " + currentId + " was not found", net);
            Assert.assertTrue(em.contains(net));

            log.log(Level.INFO, "PGinetEntity with ID {0} ({1}) has been found :)",
                    new Object[] {currentId, net.getNetwork().getValue()});
            
            // testing query...
            em.getTransaction().begin();

            String table = PGinetEntity.class.getAnnotation(Table.class).name();
            String column = PGinetEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();
            Query q = em.createNativeQuery("SELECT b.* FROM " + table + " b WHERE b." + column + " >>= #IPADDR");
            q.setParameter("IPADDR", new PGinet("10.10.1.6"));
            List networks = q.getResultList();
            
            em.getTransaction().commit();
            Assert.assertEquals("Number of found PGinetEntities", 1, networks.size());
            
            log.warning("Using workaround for EclipseLink bug #321649");
            // net = (PGinetEntity) networks.get(0);
            // Assert.assertTrue(em.contains(net));
            Object[] o = (Object[]) networks.get(0);
            net = new PGinetEntity(((PGobject) o[1]).getValue());
            net.setId((Long) o[0]);

            log.log(Level.INFO, "PGinetEntity with ID {0} ({1}) has been found :)",
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

    @Test
    public void testAdvancedEntity() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();

            AdvancedNetworkEntity net = new AdvancedNetworkEntity(new IPNetwork("10.10.0.0/16"));
            
            em.persist(net);
            Assert.assertTrue(em.contains(net));

            em.getTransaction().commit();
            Assert.assertNotNull("AdvancedNetworkEntity with PGinet "
                    + net.getNetwork().getValue() + " was not persisted",
                    net.getId());

            log.log(Level.INFO, "AdvancedNetworkEntity with PGinet {0} has been persisted :)",
                    net.getNetwork().getValue());

            Long currentId = net.getId();
            
            // clear cache...
            em.detach(net);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(net));

            // testing find...
            em.getTransaction().begin();

            net = em.find(AdvancedNetworkEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("AdvancedNetworkEntity with ID " + currentId + " was not found", net);
            Assert.assertTrue(em.contains(net));

            log.log(Level.INFO, "AdvancedNetworkEntity with ID {0} ({1}) has been found :)",
                    new Object[] {currentId, net.getNetwork().getValue()});
            
            // testing query...
            em.getTransaction().begin();

            String table = AdvancedNetworkEntity.class.getAnnotation(Table.class).name();
            String column = AdvancedNetworkEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();
            Query q = em.createNativeQuery("SELECT b.c_id, b.c_network FROM " + table + " b WHERE b." + column + " >>= #IPADDR");
            q.setParameter("IPADDR", new IPNetwork("10.10.1.6"));
            List networks = q.getResultList();
            
            em.getTransaction().commit();
            Assert.assertEquals("Number of found AdvancedNetworkEntities", 1, networks.size());
            
            log.warning("Using workaround for EclipseLink bug #321649");
            // net = (AdvancedNetworkEntity) networks.get(0);
            // Assert.assertTrue(em.contains(net));
            Object[] o = (Object[]) networks.get(0);
            net = new AdvancedNetworkEntity();
            net.setId((Long) o[0]);
            net.setNetwork(new IPNetwork(((PGobject) o[1]).getValue()));


            log.log(Level.INFO, "AdvancedNetworkEntity with ID {0} ({1}) has been found :)",
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
    
    //@Test
    public void testReadWriteStress() throws Exception {
        EntityManager em = emFactory.createEntityManager();

        String[] validAddresses = {
			"0.0.0.0",
			"192.168.1.10/32",
			"192.168.1.10/20",
			"10.10.10.120/8",
			"132.235.215.243",
			"200.46.204.71",
			"::",
			"::/32",
			"abcd:eff0::/28",
			"abcd:efef::/32",
			"4bc:ab:1234::bcda/127",
			"4bc:ab:1234::bcda/128",
			"1234::1234",
			"1234:1234:1234:1234:1234:1234:1234:123E/127",
			"1234:1234:1234:1234:1234:1234:1234:123F/127",
			"1234:1234:1234:1234:1234:1234:1234:123F/128",
			"::192.168.1.1"
		};

        int count = 10000;
        long start = 0, end = 0;
        
        try {
            start = System.currentTimeMillis();
            em.getTransaction().begin();

            for(int i=0; i<count; i++) {
                int index = (int) Math.round(Math.random() * (validAddresses.length - 1));
                String addr = validAddresses[index];
                AdvancedNetworkEntity net = new AdvancedNetworkEntity(new IPNetwork(addr));
                em.persist(net);
            }

            em.getTransaction().commit();

            end = System.currentTimeMillis();
            
            log.log(Level.INFO, "Saved {0} entities in {1} milliseconds",
                    new Object[] {count, end - start});
            
            em.getEntityManagerFactory().getCache().evictAll();
            
            start = System.currentTimeMillis();

            TypedQuery<AdvancedNetworkEntity> q = em.createQuery("SELECT n FROM AdvancedNetworkEntity n", AdvancedNetworkEntity.class);
            List<AdvancedNetworkEntity> res = q.getResultList();

            end = System.currentTimeMillis();
            
            log.log(Level.INFO, "Loaded {0} entities in {1} milliseconds",
                    new Object[] {res.size(), end - start});
            for(AdvancedNetworkEntity entity : res) {
                em.detach(entity);
                entity = null;
            }
            res = null;
            em.getEntityManagerFactory().getCache().evictAll();
            
            start = System.currentTimeMillis();

            String table = AdvancedNetworkEntity.class.getAnnotation(Table.class).name();
            String column = AdvancedNetworkEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();
            Query nq = em.createNativeQuery("SELECT * FROM " + table + " WHERE " + column + " >>= inet '192.168.1.10'", AdvancedNetworkEntity.class);
            res = nq.getResultList();

            end = System.currentTimeMillis();
            
            log.log(Level.INFO, "Loaded {0} matching entities in {1} milliseconds",
                    new Object[] {res.size(), end - start});
            for(AdvancedNetworkEntity entity : res) {
                em.detach(entity);
                entity = null;
            }
            res = null;
            em.getEntityManagerFactory().getCache().evictAll();
        } catch(Exception x) {
            if(em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            throw x;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
