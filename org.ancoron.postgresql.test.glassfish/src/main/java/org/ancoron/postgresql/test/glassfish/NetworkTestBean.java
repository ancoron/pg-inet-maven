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
        NetworkTestEntity entity = new NetworkTestEntity("192.168.1.1/24");

        Long id = null;
        try {
            // try to save...
            log.info("Persisting network test entity...");
            utx.begin();
            em.persist(entity);
            utx.commit();
            
            id = entity.getId();
            log.log(Level.INFO, "Entity got ID {0}", id);
        } catch(Exception x) {
            log.log(Level.WARNING, "Test failed:", x);
            try {
                utx.rollback();
            } catch(Exception e) {}
        }

        if(id == null) {
            return;
        }

        try {
            // try to save...
            log.info("Loading network test entity using EntityManager...");
            utx.begin();
            entity = em.find(NetworkTestEntity.class, id);
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
            
            String table = NetworkTestEntity.class.getAnnotation(Table.class).name();
            String column = NetworkTestEntity.class.getDeclaredField("network").getAnnotation(Column.class).name();

            utx.begin();
            Query query = em.createNativeQuery("SELECT e.c_id, e.c_network FROM "
                    + table + " e WHERE e." + column + " >>= #IPADDR");
            query.setParameter("IPADDR", entity.getNetwork().getNet());
            
            // entity = (NetworkTestEntity) query.getSingleResult();
            Object[] res = (Object[]) query.getSingleResult();
            entity = new NetworkTestEntity(((PGobject) res[1]).getValue());
            entity.setId((Long) res[0]);
            utx.commit();
            
            log.log(Level.INFO, "Got entity {0}", entity);
        } catch(Exception x) {
            log.log(Level.WARNING, "Test failed:", x);
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
