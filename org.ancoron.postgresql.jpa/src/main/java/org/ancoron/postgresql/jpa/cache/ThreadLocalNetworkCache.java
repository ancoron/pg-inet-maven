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
package org.ancoron.postgresql.jpa.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author ancoron
 */
public class ThreadLocalNetworkCache extends NetworkCache {
    
    private static final Logger log = Logger.getLogger(ThreadLocalNetworkCache.class.getName());

    private static final InheritableThreadLocal<ThreadLocalNetworkCache> instance =
            new InheritableThreadLocal<ThreadLocalNetworkCache>();
    
    private Map<String, Object[]> internal = new HashMap<String, Object[]>(100);

    public static ThreadLocalNetworkCache getInstance() {
        if(instance.get() == null) {
            instance.set(new ThreadLocalNetworkCache());
        }
        
        return instance.get();
    }
    
    @Override
    public Object[] get(String net) {
        Object[] res = internal.get(net);

        if (res == null) {
            res = new Object[5];
        }

        return res;
    }

    @Override
    public void put(String net, Object[] properties) {
        internal.put(net, properties);
    }
}
