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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.junit.Test;
import org.junit.Assert;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
public class NoConverterTest extends AbstractTestBase {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(NoConverterTest.class.getName());
    }

    @Test
    public void testNoAnnotation() throws Exception {
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
            doThrow(ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Test
    public void testInheritance() throws Exception {
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
            doThrow(ex);
        }
    }

    @Test
    public void testMethodLevelAnnotation() throws Exception {
        try {
            // testing persist() ...
            em.getTransaction().begin();
            UUID uuid = UUID.randomUUID();
            NoConverterMethodEntity nc = createNoConvM(uuid, "10.66.0.0/16",
                    "10.67.0.0/16", "10.66.2.3", "10.66.2.4", "36:3a:ec:89:a6:19");
            
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
            doThrow(ex);
        }
    }

    protected NoConverterMethodEntity createNoConvM(UUID uuid, String cidr,
            String network, String inet, String ip, String mac)
            throws SQLException, UnknownHostException
    {
        NoConverterMethodEntity nc = new NoConverterMethodEntity();
        nc.setCidr(new PGcidr(cidr));
        nc.setNetwork(new IPNetwork(network));
        nc.setInet(new PGinet(inet));
        nc.setInetAddr(InetAddress.getLocalHost());
        nc.setTarget(new IPTarget(ip));
        nc.setMacaddr(new PGmacaddr(mac));
        nc.setUuid(uuid);
        return nc;
    }

    @Test
    public void testUUIDPrimaryKey() throws Exception {
        try {
            // testing persist() ...
            em.getTransaction().begin();

            UUID uuid = UUID.randomUUID();
            UUID ncm = UUID.randomUUID();
            UUIDPKEntity u = new UUIDPKEntity();
            u.setUuid(uuid);
            u.setName("test-uuid");
            u.setNcm(createNoConvM(ncm, "11.66.0.0/16", "11.67.0.0/16",
                    "11.66.2.3", "11.66.2.4", "37:3a:ec:89:a6:19"));
            
            em.persist(u);
            Assert.assertTrue(em.contains(u));

            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity with name "
                    + u.getName() + " was not persisted",
                    u.getUuid());

            log.log(Level.INFO, "UUIDPKEntity with name {0} has been persisted: {1}",
                    new Object[] {u.getName(), u.getUuid()});

            UUID currentId = u.getUuid();
            
            // clear cache...
            em.detach(u);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(u));
            u = null;

            // testing find...
            em.getTransaction().begin();

            u = em.find(UUIDPKEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity with UUID " + currentId + " was not found", u);
            Assert.assertTrue(em.contains(u));
            Assert.assertEquals("test-uuid", u.getName());

            log.log(Level.INFO, "UUIDPKEntity with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, u.getName()});
            doSingleResultTest(em, UUIDPKEntity.class, "name", "test-uuid");

            NoConverterMethodEntity ne = u.getNcm();
            Assert.assertNotNull("UUID reference failed", ne);

            Assert.assertNotNull("UUID reference failed on UUID", ne.getUuid());

            log.log(Level.INFO, "UUIDPKEntity with ID {0} ({1}) references NoConverterMethodEntity with UUID {2}",
                    new Object[] {currentId, u.getName(), ne.getUuid()});

            // testing merge ...
            em.getTransaction().begin();
            u.setName("new-name");
            em.merge(u);
            em.getTransaction().commit();
            
            // clear cache...
            em.detach(u);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(u));
            u = null;

            // testing find...
            em.getTransaction().begin();

            u = em.find(UUIDPKEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity with UUID " + currentId + " was not found", u);
            Assert.assertTrue(em.contains(u));
            Assert.assertEquals("new-name", u.getName());

            log.log(Level.INFO, "UUIDPKEntity with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, u.getName()});
            doSingleResultTest(em, UUIDPKEntity.class, "name", "new-name");


            // Test many-to-one/one-to-many ...
            UUIDReferenceEntity ref = new UUIDReferenceEntity();
            ref.setId(UUID.randomUUID());
            ref.setReference(u);
            u.getRefs().add(ref);
            
            em.getTransaction().begin();
            em.persist(ref);
            Assert.assertTrue(em.contains(ref));

            em.getTransaction().commit();
            Assert.assertNotNull("UUIDReferenceEntity was not persisted",
                    ref.getId());

            log.log(Level.INFO, "UUIDReferenceEntity has been persisted: {0}",
                    new Object[] {ref.getId()});
            
            currentId = ref.getId();

            // clear cache...
            em.detach(ref);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(ref));
            ref = null;

            // testing find...
            em.getTransaction().begin();

            ref = em.find(UUIDReferenceEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDReferenceEntity with ID " + currentId + " was not found", ref);
            Assert.assertTrue(em.contains(ref));

            Assert.assertEquals("Referenced NoConverterMethodEntity not as expected",
                    ncm, ref.getReference().getNcm().getUuid());

            log.log(Level.INFO, "UUIDReferenceEntity references UUIDPKEntity {0} and NoConverterMethodEntity {1}",
                    new Object[] {ref.getReference().getUuid(), ref.getReference().getNcm().getUuid()});
        } catch (Exception ex) {
            doThrow(ex);
        }
    }


    @Test
    public void testUUIDPrimaryKey2() throws Exception {
        try {
            // testing persist() ...
            em.getTransaction().begin();

            UUID uuid = UUID.randomUUID();
            UUID ncm = UUID.randomUUID();
            UUIDPKEntity2 u = new UUIDPKEntity2();
            u.setId(uuid);
            u.setName("test-uuid-2");
            u.setNcm(createNoConvM(ncm, "11.68.0.0/16", "11.68.0.0/16",
                    "11.68.2.3", "11.68.2.4", "37:3a:ec:89:a6:20"));
            
            em.persist(u);
            Assert.assertTrue(em.contains(u));

            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity2 with name "
                    + u.getName() + " was not persisted",
                    u.getId());

            log.log(Level.INFO, "UUIDPKEntity2 with name {0} has been persisted: {1}",
                    new Object[] {u.getName(), u.getId()});

            // create a copy...
            UUID currentId = u.getId();
            
            // clear cache...
            em.detach(u);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(u));
            u = null;

            // testing find...
            em.getTransaction().begin();

            u = em.find(UUIDPKEntity2.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity2 with UUID " + currentId + " was not found", u);
            Assert.assertTrue(em.contains(u));
            Assert.assertEquals("test-uuid-2", u.getName());

            log.log(Level.INFO, "UUIDPKEntity2 with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, u.getName()});
            doSingleResultTest(em, UUIDPKEntity2.class, "name", "test-uuid-2");

            NoConverterMethodEntity ne = u.getNcm();
            Assert.assertNotNull("UUID reference failed", ne);

            Assert.assertNotNull("UUID reference failed on UUID", ne.getUuid());

            log.log(Level.INFO, "UUIDPKEntity2 with ID {0} ({1}) references NoConverterMethodEntity with UUID {2}",
                    new Object[] {currentId, u.getName(), ne.getUuid()});

            // testing merge ...
            em.getTransaction().begin();
            u.setName("new-name-2");
            em.merge(u);
            em.getTransaction().commit();
            
            // clear cache...
            em.detach(u);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(u));
            u = null;

            // testing find...
            em.getTransaction().begin();

            u = em.find(UUIDPKEntity2.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDPKEntity2 with UUID " + currentId + " was not found", u);
            Assert.assertTrue(em.contains(u));
            Assert.assertEquals("new-name-2", u.getName());

            log.log(Level.INFO, "UUIDPKEntity2 with ID {0} ({1}) has been found (by ID)",
                    new Object[] {currentId, u.getName()});
            doSingleResultTest(em, UUIDPKEntity2.class, "name", "new-name-2");


            // Test many-to-one/one-to-many ...
            UUIDReferenceEntity2 ref = new UUIDReferenceEntity2();
            ref.setId(UUID.randomUUID());
            ref.setReference(u);
            u.getRefs().add(ref);
            
            em.getTransaction().begin();
            em.persist(ref);
            Assert.assertTrue(em.contains(ref));

            em.getTransaction().commit();
            Assert.assertNotNull("UUIDReferenceEntity2 was not persisted",
                    ref.getId());

            log.log(Level.INFO, "UUIDReferenceEntity2 has been persisted: {0}",
                    new Object[] {ref.getId()});
            
            currentId = ref.getId();

            // clear cache...
            em.detach(ref);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(ref));
            ref = null;

            // testing find...
            em.getTransaction().begin();

            ref = em.find(UUIDReferenceEntity2.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDReferenceEntity2 with ID " + currentId + " was not found", ref);
            Assert.assertTrue(em.contains(ref));

            Assert.assertEquals("Referenced NoConverterMethodEntity not as expected",
                    ncm, ref.getReference().getNcm().getUuid());

            log.log(Level.INFO, "UUIDReferenceEntity2 references UUIDPKEntity2 {0} and NoConverterMethodEntity {1}",
                    new Object[] {ref.getReference().getId(), ref.getReference().getNcm().getUuid()});
        } catch (Exception ex) {
            doThrow(ex);
        }
    }

    @Test
    public void testUUIDNullRef() throws Exception {
        try {
            UUID currentId = UUID.randomUUID();

            // Test many-to-one/one-to-many ...
            UUIDNullableRefEntity ref = new UUIDNullableRefEntity();
            ref.setId(UUID.randomUUID());
            ref.setReference(null);
            
            // testing persist() ...
            em.getTransaction().begin();
            em.persist(ref);
            Assert.assertTrue(em.contains(ref));

            em.getTransaction().commit();
            Assert.assertNotNull("UUIDNullableRefEntity was not persisted",
                    ref.getId());

            log.log(Level.INFO, "UUIDNullableRefEntity has been persisted: {0}",
                    new Object[] {ref.getId()});
            
            currentId = ref.getId();

            // clear cache...
            em.detach(ref);
            em.getEntityManagerFactory().getCache().evictAll();
            Assert.assertFalse(em.contains(ref));
            ref = null;

            // testing find...
            em.getTransaction().begin();

            ref = em.find(UUIDNullableRefEntity.class, currentId);
            
            em.getTransaction().commit();
            Assert.assertNotNull("UUIDNullableRefEntity with ID " + currentId + " was not found", ref);
            Assert.assertTrue(em.contains(ref));
            Assert.assertNull(ref.getReference());

            // update 1 ...
            UUIDPKEntity2 e = new UUIDPKEntity2();
            e.setId(UUID.randomUUID());
            e.setName("test-nullable");
            NoConverterMethodEntity ncm = new NoConverterMethodEntity();
            e.setNcm(ncm);
            
            em.getTransaction().begin();
            ref.setReference(e);
            ref = em.merge(ref);
            em.getTransaction().commit();

            // update 2 ...
            em.getTransaction().begin();
            ref.setReference(null);
            ref = em.merge(ref);
            em.getTransaction().commit();

            // remove...
            em.getTransaction().begin();
            em.remove(ref);
            Assert.assertFalse(em.contains(ref));
            em.getTransaction().commit();
        } catch (Exception ex) {
            doThrow(ex);
        }
    }
}
