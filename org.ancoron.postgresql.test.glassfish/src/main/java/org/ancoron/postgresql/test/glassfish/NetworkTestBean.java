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
package org.ancoron.postgresql.test.glassfish;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.transaction.UserTransaction;
import org.postgresql.net.PGinet;
import org.postgresql.util.PGobject;

/**
 *
 * @author ancoron
 */
@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class NetworkTestBean {
    
    private static final Logger log = Logger.getLogger(NetworkTestBean.class.getName());

    @PersistenceContext(name="NetworkTestPU")
    private EntityManager em;
    
    @Resource
    private TimerService timerService;
    
    @Resource
    private UserTransaction utx;
    
    @Timeout
    protected void startTests()
    {
        String table = NetworkTestEntity.class.getAnnotation(Table.class).name();

        NetworkTestEntity entity = new NetworkTestEntity("192.168.1.0/24");
        entity.setUuid(UUID.randomUUID().toString());

        String uuid = null;
        try {
            // try to save...
            log.info("Persisting network test entity...");
            utx.begin();
            em.persist(entity);
            utx.commit();
            
            uuid = entity.getUuid();
            log.log(Level.INFO, "Entity with UUID {0} has been persisted", uuid);
        } catch(Exception x) {
            log.log(Level.WARNING, "Test failed:", x);
            try {
                utx.rollback();
            } catch(Exception e) {}
        }

        if(uuid == null) {
            return;
        }

        try {
            // try to save...
            log.info("Loading network test entity using EntityManager...");
            utx.begin();
            entity = em.find(NetworkTestEntity.class, uuid);
            utx.commit();
            
            log.log(Level.INFO, "Got entity {0}", entity);
        } catch(Exception x) {
            log.log(Level.WARNING, "Test failed:", x);
            try {
                utx.rollback();
            } catch(Exception e) {}
        }

        try {
            // try to save...
            log.info("Loading network test entity using native query...");
            
            String column = NetworkTestEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();

            utx.begin();
            Query query = em.createNativeQuery("SELECT e.c_uuid, e.c_network FROM "
                    + table + " e WHERE e." + column + " >>= #IPADDR");
            query.setParameter("IPADDR", new PGinet("192.168.1.25"));
            
            // entity = (NetworkTestEntity) query.getSingleResult();
            Object[] res = (Object[]) query.getSingleResult();
            entity = new NetworkTestEntity(((PGobject) res[1]).getValue());
            entity.setUuid((String) res[0]);
            utx.commit();
            
            log.log(Level.INFO, "Got entity {0}", entity);
        } catch(Exception x) {
            log.log(Level.WARNING, "Test failed:", x);
            try {
                utx.rollback();
            } catch(Exception e) {}
        }
        
        // cleanup after test...
        try {
            utx.begin();
            em.createNativeQuery("DROP TABLE " + table).executeUpdate();
            utx.commit();
            
            log.log(Level.INFO, "Cleanup done.");
        } catch(Exception x) {
            log.log(Level.WARNING, "Cleanup failed!", x);
            try {
                utx.rollback();
            } catch(Exception e) {}
        }
    }
    
    @PostConstruct
    protected void init()
    {
        timerService.createSingleActionTimer(3000, new TimerConfig());
    }
}
