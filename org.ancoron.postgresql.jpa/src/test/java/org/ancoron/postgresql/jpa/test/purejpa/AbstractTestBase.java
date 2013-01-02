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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.ancoron.postgresql.jpa.test.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.postgresql.net.Driver;

/**
 *
 * @author ancoron
 */
public abstract class AbstractTestBase {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(AbstractTestBase.class.getName());
    }

    protected static EntityManagerFactory emFactory;

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

    protected EntityManager em;
    
    @Before
    public void before() {
        em = emFactory.createEntityManager();
    }

    @After
    public void after() {
        if(em != null) {
            try {
                if(em.getTransaction() != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            } finally {
                if(em.isOpen()) {
                    em.close();
                }
                em = null;
            }
        }
    }

    protected <T extends Serializable> void doSingleResultTest(EntityManager em, Class<T> c, String attribute, Object param) throws NoSuchFieldException, SecurityException {
        // testing query...
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT b FROM " + c.getSimpleName() + " b WHERE b." + attribute + " = :VAL", NoConverterEntity.class);
        q.setParameter("VAL", param);
        List networks = q.getResultList();
        em.getTransaction().commit();
        Assert.assertEquals("Number of found " + c.getSimpleName() + " instances", 1, networks.size());
        T nc = (T) networks.get(0);
        Assert.assertTrue(em.contains(nc));
        log.log(Level.INFO, "{0} found by query where {1} = {2} (a {3})", new Object[]{c.getSimpleName(), attribute, String.valueOf(param), param.getClass().getName()});
    }

    protected void doThrow(Exception ex) throws Exception {
        SQLException sx = null;
        Throwable e = ex;
        while (sx == null && e.getCause() != null) {
            e = e.getCause();
            if (e instanceof SQLException) {
                sx = (SQLException) e;
                break;
            }
        }
        if (sx != null && sx.getNextException() != null) {
            throw sx.getNextException();
        }
        throw ex;
    }
    
}
