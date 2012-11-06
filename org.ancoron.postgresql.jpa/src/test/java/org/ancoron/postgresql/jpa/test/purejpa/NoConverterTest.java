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
package org.ancoron.postgresql.jpa.test.purejpa;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Table;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.ancoron.postgresql.jpa.test.TestUtil;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.postgresql.net.Driver;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
public class NoConverterTest {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(NoConverterTest.class.getName());
    }

    private static EntityManagerFactory emFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        log.info("Building JPA EntityManager for unit tests");

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("javax.persistence.jdbc.url", TestUtil.getPGJDBCUrl());
        properties.put("javax.persistence.jdbc.driver", Driver.class.getName());
        properties.put("javax.persistence.jdbc.user", TestUtil.getPGUser());
        properties.put("javax.persistence.jdbc.password", TestUtil.getPGPassword());

        emFactory = Persistence.createEntityManagerFactory("noconv-test-unit", properties);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        log.info("Shuting down JPA layer.");
        if (emFactory != null) {
            emFactory.close();
        }
    }

    @Test
    public void testNoAnnotation() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();

            NoConverterEntity nc = new NoConverterEntity();
            nc.cidr = new PGcidr("10.66.0.0/16");
            nc.network = new IPNetwork("10.67.0.0/16");
            nc.inet = new PGinet("10.66.2.3");
            nc.target = new IPTarget("10.66.2.4");
            nc.macaddr = new PGmacaddr("36:3a:ec:89:a6:19");
            
            em.persist(nc);
            Assert.assertTrue(em.contains(nc));

            em.getTransaction().commit();
            Assert.assertNotNull("NoConverterEntity with PGinet "
                    + nc.inet.getValue() + " was not persisted",
                    nc.id);

            log.log(Level.INFO, "NoConverterEntity with PGinet {0} has been persisted :)",
                    nc.inet.getValue());

            Long currentId = nc.id;
            
            // clear cache...
            em.detach(nc);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(nc));

            // testing find...
            em.getTransaction().begin();

            nc = em.find(NoConverterEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("NoConverterEntity with ID " + currentId + " was not found", nc);
            Assert.assertTrue(em.contains(nc));

            log.log(Level.INFO, "NoConverterEntity with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, nc.inet.getValue()});

            doSingleResultTest(em, NoConverterEntity.class, "cidr", new PGcidr("10.66.0.0/16"));
            doSingleResultTest(em, NoConverterEntity.class, "network", new IPNetwork("10.67.0.0/16"));
            doSingleResultTest(em, NoConverterEntity.class, "inet", new PGinet("10.66.2.3"));
            doSingleResultTest(em, NoConverterEntity.class, "target", new IPTarget("10.66.2.4"));
            doSingleResultTest(em, NoConverterEntity.class, "macaddr", new PGmacaddr("36:3a:ec:89:a6:19"));
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
    public void testInheritance() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();

            InheritedNoConvEntity nc = new InheritedNoConvEntity();
            nc.cidr = new PGcidr("10.66.0.0/16");
            nc.macaddr = new PGmacaddr("36:3a:ec:89:a6:19");
            nc.inetAddr = InetAddress.getByName("10.66.2.3");
            nc.uuid = UUID.randomUUID();

            nc.dnsServers.add(InetAddress.getByName("10.66.0.1"));
            nc.dnsServers.add(InetAddress.getByName("10.66.0.13"));
            nc.dnsServers.add(InetAddress.getByName("8.8.8.8"));

            nc.arp.put(new PGmacaddr("6c:f0:49:53:43:74"), new PGinet("10.66.0.1"));
            nc.arp.put(new PGmacaddr("00:0e:0c:33:d2:04"), new PGinet("10.66.0.13"));
            nc.arp.put(new PGmacaddr("c2:17:d3:af:98:7b"), new PGinet("8.8.8.8"));
            
            em.persist(nc);
            Assert.assertTrue(em.contains(nc));

            em.getTransaction().commit();
            Assert.assertNotNull("InheritedNoConvEntity with PGmacaddr "
                    + nc.macaddr.getValue() + " was not persisted",
                    nc.id);

            log.log(Level.INFO, "InheritedNoConvEntity with PGmacaddr {0} has been persisted :)",
                    nc.macaddr.getValue());

            Long currentId = nc.id;
            
            // clear cache...
            em.detach(nc);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(nc));

            // testing find...
            em.getTransaction().begin();

            nc = em.find(InheritedNoConvEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("InheritedNoConvEntity with ID " + currentId + " was not found", nc);
            Assert.assertNotNull("UUUID missing for InheritedNoConvEntity with ID " + currentId, nc.uuid);
            Assert.assertNotNull("PGmacaddr missing for InheritedNoConvEntity with ID " + currentId, nc.macaddr);
            Assert.assertNotNull("PGcidr missing for InheritedNoConvEntity with ID " + currentId, nc.cidr);
            Assert.assertNotNull("InetAddress missing for InheritedNoConvEntity with ID " + currentId, nc.inetAddr);
            Assert.assertEquals("Wrong number of DNS servers for InheritedNoConvEntity with ID " + currentId,
                    3, nc.dnsServers.size());
            Assert.assertEquals("Wrong number of ARP entries for InheritedNoConvEntity with ID " + currentId,
                    3, nc.arp.size());
            Assert.assertTrue("Missing ARP entry for InheritedNoConvEntity with ID " + currentId,
                    nc.arp.containsKey(new PGmacaddr("6c:f0:49:53:43:74")));
            Assert.assertTrue("Missing ARP entry for InheritedNoConvEntity with ID " + currentId,
                    nc.arp.containsKey(new PGmacaddr("00:0e:0c:33:d2:04")));
            Assert.assertTrue("Missing ARP entry for InheritedNoConvEntity with ID " + currentId,
                    nc.arp.containsKey(new PGmacaddr("c2:17:d3:af:98:7b")));
            Assert.assertEquals("Wrong ARP entry for InheritedNoConvEntity with ID " + currentId,
                    new PGinet("10.66.0.1"), nc.arp.get(new PGmacaddr("6c:f0:49:53:43:74")));
            Assert.assertEquals("Wrong ARP entry for InheritedNoConvEntity with ID " + currentId,
                    new PGinet("10.66.0.13"), nc.arp.get(new PGmacaddr("00:0e:0c:33:d2:04")));
            Assert.assertEquals("Wrong ARP entry for InheritedNoConvEntity with ID " + currentId,
                    new PGinet("8.8.8.8"), nc.arp.get(new PGmacaddr("c2:17:d3:af:98:7b")));
            Assert.assertTrue(em.contains(nc));

            log.log(Level.INFO, "InheritedNoConvEntity with ID {0} ({1}) has been found (by ID)",
                    new Object[] {nc.id, nc.macaddr.getValue()});

            doSingleResultTest(em, InheritedNoConvEntity.class, "cidr", new PGcidr("10.66.0.0/16"));
            doSingleResultTest(em, InheritedNoConvEntity.class, "inetAddr", InetAddress.getByName("10.66.2.3"));
            doSingleResultTest(em, InheritedNoConvEntity.class, "macaddr", new PGmacaddr("36:3a:ec:89:a6:19"));
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
    public void testMethodLevelAnnotation() throws Exception {
        EntityManager em = emFactory.createEntityManager();
        try {
            // testing persist() ...
            em.getTransaction().begin();
            UUID uuid = UUID.randomUUID();

            NoConverterMethodEntity nc = new NoConverterMethodEntity();
            nc.setCidr(new PGcidr("10.66.0.0/16"));
            nc.setNetwork(new IPNetwork("10.67.0.0/16"));
            nc.setInet(new PGinet("10.66.2.3"));
            nc.setInetAddr(InetAddress.getLocalHost());
            nc.setTarget(new IPTarget("10.66.2.4"));
            nc.setMacaddr(new PGmacaddr("36:3a:ec:89:a6:19"));
            nc.setUuid(uuid);
            
            em.persist(nc);
            Assert.assertTrue(em.contains(nc));

            em.getTransaction().commit();
            Assert.assertNotNull("NoConverterMethodEntity with PGinet "
                    + nc.getInet().getValue() + " was not persisted",
                    nc.getId());

            log.log(Level.INFO, "NoConverterMethodEntity with PGinet {0} has been persisted :)",
                    nc.getInet().getValue());

            Long currentId = nc.getId();
            
            // clear cache...
            em.detach(nc);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(nc));

            // testing find...
            em.getTransaction().begin();

            nc = em.find(NoConverterMethodEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("NoConverterMethodEntity with ID " + currentId + " was not found", nc);
            Assert.assertTrue(em.contains(nc));
            Assert.assertEquals(uuid.toString(), nc.getUuid().toString());

            log.log(Level.INFO, "NoConverterMethodEntity with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, nc.getInet().getValue()});
            doSingleResultTest(em, NoConverterMethodEntity.class, "cidr", new PGcidr("10.66.0.0/16"));
            doSingleResultTest(em, NoConverterMethodEntity.class, "network", new IPNetwork("10.67.0.0/16"));
            doSingleResultTest(em, NoConverterMethodEntity.class, "inet", new PGinet("10.66.2.3"));
            doSingleResultTest(em, NoConverterMethodEntity.class, "target", new IPTarget("10.66.2.4"));
            doSingleResultTest(em, NoConverterMethodEntity.class, "macaddr", new PGmacaddr("36:3a:ec:89:a6:19"));
            doSingleResultTest(em, NoConverterMethodEntity.class, "uuid", uuid);
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

    protected <T extends Serializable> void doSingleResultTest(EntityManager em, Class<T> c, String attribute, Object param) throws NoSuchFieldException, SecurityException {
        // testing query...
        em.getTransaction().begin();

        Query q = em.createQuery("SELECT b FROM " + c.getSimpleName()
                + " b WHERE b." + attribute + " = :VAL",
                NoConverterEntity.class);
        q.setParameter("VAL", param);
        List networks = q.getResultList();

        em.getTransaction().commit();

        Assert.assertEquals("Number of found " + c.getSimpleName() + " instances",
                1, networks.size());

        T nc = (T) networks.get(0);

        Assert.assertTrue(em.contains(nc));

        log.log(Level.INFO, "{0} found by query where {1} = {2} (a {3})",
                new Object[] {c.getSimpleName(), attribute,
                    String.valueOf(param), param.getClass().getName()});
    }
}
