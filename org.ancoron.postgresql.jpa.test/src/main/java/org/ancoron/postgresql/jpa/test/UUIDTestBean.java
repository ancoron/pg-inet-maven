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

import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author ancoron
 */
@Stateless
@EJB(name = "java:global/UUIDTestBean", beanInterface = UUIDTestBeanLocal.class)
public class UUIDTestBean implements UUIDTestBeanLocal {

    @PersistenceContext(unitName = "test-pu")
    private EntityManager em;

    @Override
    public UUID addUUID(String name) {
        UUID id = UUID.randomUUID();
        UUIDTestEntity uuid = new UUIDTestEntity();
        uuid.setId(id);
        uuid.setName(name);
        em.persist(uuid);
        return id;
    }

    @Override
    public UUIDTestEntity findByUUID(UUID uuid) {
        return em.find(UUIDTestEntity.class, uuid);
    }

    @Override
    public UUIDTestEntity deleteUUID(UUID uuid) {
        UUIDTestEntity entity = findByUUID(uuid);
        em.remove(entity);
        return entity;
    }

    @Override
    public UUIDTestEntity findByName(String name) {
        TypedQuery<UUIDTestEntity> q = em.createNamedQuery(UUIDTestEntity.QUERY_FIND_BY_NAME, UUIDTestEntity.class);
        q.setParameter("NAME", name);
        return q.getSingleResult();
    }

    @Override
    public List<UUID> findAllIds() {
        Query q = em.createQuery("SELECT u.id FROM " + UUIDTestEntity.class.getSimpleName() + " u");
        return q.getResultList();
    }
}
