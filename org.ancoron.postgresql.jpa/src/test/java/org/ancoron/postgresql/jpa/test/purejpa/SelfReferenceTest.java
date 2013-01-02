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

import java.util.UUID;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ancoron
 */
public class SelfReferenceTest extends AbstractTestBase {

    private SelfReferenceUUIDEntity entity;

    @Before
    public void initialize() {
        entity = new SelfReferenceUUIDEntity(UUID.randomUUID(), "self-reference");
    }

    @After
    public void cleanup() {
        entity = null;
    }

    @Test
    public void persist() throws Exception {
        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });
    }

    @Test
    public void persistSingleTargetOneShot() {
        final SelfReferenceUUIDEntity e = new SelfReferenceUUIDEntity(UUID.randomUUID(), "singleTarget");
        entity.addTarget(e);

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        SelfReferenceUUIDEntity found = transactional(
                new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.find(SelfReferenceUUIDEntity.class, entity.getUuid());
            }
        });

        Assert.assertNotNull(found);

        Assert.assertNotNull(found.getSources());
        Assert.assertTrue(found.getSources().isEmpty());

        Assert.assertNotNull(found.getTargets());
        Assert.assertFalse(found.getTargets().isEmpty());
        Assert.assertEquals(1, found.getTargets().size());

        Assert.assertEquals(e.getUuid(), found.getTargets().iterator().next().getUuid());

        found = transactional(
                new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.find(SelfReferenceUUIDEntity.class, e.getUuid());
            }
        });

        Assert.assertNotNull(found);

        Assert.assertNotNull(found.getTargets());
        Assert.assertTrue(found.getTargets().isEmpty());

        Assert.assertNotNull(found.getSources());
        Assert.assertFalse(found.getSources().isEmpty());
        Assert.assertEquals(1, found.getSources().size());
        Assert.assertEquals(entity.getUuid(), found.getSources().iterator().next().getUuid());
    }

    @Test
    public void persistSingleTargetWithUpdate() throws Exception {

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        final SelfReferenceUUIDEntity e = new SelfReferenceUUIDEntity(UUID.randomUUID(), "singleTargetUpdate");
        entity.addTarget(e);

        entity = transactional(new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.merge(entity);
            }
        });

        SelfReferenceUUIDEntity found = transactional(
                new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.find(SelfReferenceUUIDEntity.class, entity.getUuid());
            }
        });

        Assert.assertNotNull(found);

        Assert.assertNotNull(found.getSources());
        Assert.assertTrue(found.getSources().isEmpty());

        Assert.assertNotNull(found.getTargets());
        Assert.assertFalse(found.getTargets().isEmpty());
        Assert.assertEquals(1, found.getTargets().size());

        Assert.assertEquals(e.getUuid(), found.getTargets().iterator().next().getUuid());

        found = transactional(
                new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.find(SelfReferenceUUIDEntity.class, e.getUuid());
            }
        });

        Assert.assertNotNull(found);

        Assert.assertNotNull(found.getTargets());
        Assert.assertTrue(found.getTargets().isEmpty());

        Assert.assertNotNull(found.getSources());
        Assert.assertFalse(found.getSources().isEmpty());
        Assert.assertEquals(1, found.getSources().size());
        Assert.assertEquals(entity.getUuid(), found.getSources().iterator().next().getUuid());
    }

    @Test
    public void cyclic() {
        final SelfReferenceUUIDEntity e = new SelfReferenceUUIDEntity(UUID.randomUUID(), "cyclic");
        entity.addTarget(e);
        e.addTarget(entity);

        transactional(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                em.persist(entity);
                return null;
            }
        });

        clearCache(e, entity);

        UUID target = e.getUuid();

        // reset...
        e.setUuid(null);
        e.setName(null);

        SelfReferenceUUIDEntity found = transactional(
                new Callable<SelfReferenceUUIDEntity>() {

            @Override
            public SelfReferenceUUIDEntity call() throws Exception {
                return em.find(SelfReferenceUUIDEntity.class, entity.getUuid());
            }
        });

        Assert.assertNotNull(found);

        Assert.assertNotNull(found.getSources());
        Assert.assertFalse(found.getSources().isEmpty());
        Assert.assertEquals(1, found.getSources().size());
        Assert.assertEquals(target, found.getSources().iterator().next().getUuid());

        Assert.assertNotNull(found.getTargets());
        Assert.assertFalse(found.getTargets().isEmpty());
        Assert.assertEquals(1, found.getTargets().size());

        Assert.assertEquals(target, found.getTargets().iterator().next().getUuid());
    }
}
