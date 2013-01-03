/*
 * Copyright 2013 ancoron.
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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ancoron
 */
public class SimpleNetworkTest extends AbstractTestBase {

    private static final AtomicInteger c = new AtomicInteger(0);
    private SimpleNetworkEntity entity;

    private String generateNetwork() {
        int i = c.incrementAndGet();
        return "10.1." + (i % 256) + ".0/24";
    }

    @Before
    public void initialize() {
        entity = new SimpleNetworkEntity(generateNetwork(), "simple-network");
    }

    @After
    public void cleanup() {
        entity = null;
    }

    @Test
    public void persist() {
        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        clearCache(entity);

        SimpleNetworkEntity found = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.find(SimpleNetworkEntity.class, entity.getNet());
            }
        });

        Assert.assertNotNull(found);
        Assert.assertEquals(entity.getNet(), found.getNet());
    }

    @Test
    public void update() {
        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        clearCache(entity);

        SimpleNICEntity nic = new SimpleNICEntity("00:00:00:12:34:56", null, "nic-update");
        entity.addDevice(nic);

        entity = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.merge(entity);
            }
        });

        clearCache(entity, nic);

        SimpleNetworkEntity found = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.find(SimpleNetworkEntity.class, entity.getNet());
            }
        });

        Assert.assertEquals(entity.getNet(), found.getNet());
        Assert.assertNotNull(found.getDevices());
        Assert.assertFalse(found.getDevices().isEmpty());
        Assert.assertEquals(nic.getMac(), found.getDevices().iterator().next().getMac());
        Assert.assertEquals(entity.getNet(), found.getDevices().iterator().next().getNetwork().getNet());
    }

    @Test
    public void persistTree() {
        SimpleNICEntity nic = new SimpleNICEntity("00:00:00:22:34:56", null, "nic-persist-tree");
        entity.addDevice(nic);

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        clearCache(entity, nic);

        SimpleNetworkEntity found = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.find(SimpleNetworkEntity.class, entity.getNet());
            }
        });

        Assert.assertEquals(entity.getNet(), found.getNet());
        Assert.assertNotNull(found.getDevices());
        Assert.assertFalse(found.getDevices().isEmpty());
        Assert.assertEquals(nic.getMac(), found.getDevices().iterator().next().getMac());
        Assert.assertEquals(entity.getNet(), found.getDevices().iterator().next().getNetwork().getNet());
    }

    @Test
    public void removeTree() {
        final SimpleNICEntity nic = new SimpleNICEntity("00:00:00:32:34:56", null, "nic-remove-tree");
        entity.addDevice(nic);

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        clearCache();

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.remove(entity);
                return null;
            }
        });

        clearCache(entity, nic);

        SimpleNetworkEntity found = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.find(SimpleNetworkEntity.class, entity.getNet());
            }
        });

        Assert.assertNull(found);

        SimpleNICEntity found2 = transactional(new Callable<SimpleNICEntity>() {

            @Override
            public SimpleNICEntity call() throws Exception {
                return em.find(SimpleNICEntity.class, nic.getMac());
            }
        });

        Assert.assertNull(found2);
    }

    @Test
    public void removeNet() {
        final SimpleNICEntity nic = new SimpleNICEntity("00:00:00:42:34:56", null, "nic-remove-net");
        entity.addDevice(nic);

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        // detach NIC from network...
        entity.removeDevice(nic);

        entity = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                SimpleNetworkEntity tmp = em.merge(entity);
                return tmp;
            }
        });

        clearCache();

        Assert.assertTrue(entity.getDevices().isEmpty());

        SimpleNICEntity found2 = transactional(new Callable<SimpleNICEntity>() {

            @Override
            public SimpleNICEntity call() throws Exception {
                return em.find(SimpleNICEntity.class, nic.getMac());
            }
        });

        Assert.assertNotNull(found2);
        Assert.assertNull(found2.getNetwork());

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.remove(entity);
                return null;
            }
        });

        clearCache(entity, nic);

        SimpleNetworkEntity found = transactional(new Callable<SimpleNetworkEntity>() {

            @Override
            public SimpleNetworkEntity call() throws Exception {
                return em.find(SimpleNetworkEntity.class, entity.getNet());
            }
        });

        Assert.assertNull(found);

        found2 = transactional(new Callable<SimpleNICEntity>() {

            @Override
            public SimpleNICEntity call() throws Exception {
                return em.find(SimpleNICEntity.class, nic.getMac());
            }
        });

        Assert.assertNotNull(found2);
        Assert.assertNull(found2.getNetwork());
    }
}
