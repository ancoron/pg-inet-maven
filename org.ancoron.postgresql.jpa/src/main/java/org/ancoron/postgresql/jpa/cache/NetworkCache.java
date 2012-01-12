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

/**
 * 
 *
 * @author ancoron
 */
public abstract class NetworkCache {

    /**
     * This method retrieves a current instance of the cache implementation.
     * 
     * @return A valid (not-null) instance of {@link NetworkCache}
     */
    public static NetworkCache getInstance() {
        throw new IllegalStateException("I forgot to override 'getInstance'");
    }
    
    public abstract Object[] get(String net);
    public abstract void put(String net, Object[] properties);
}
