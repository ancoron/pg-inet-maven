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
package org.ancoron.postgresql.jpa;

import java.util.logging.Logger;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author ancoron
 */
public class IPTargetTest {

    private static final Logger log;
    
    static {
        log = Logger.getLogger(IPTargetTest.class.getName());
    }

    @Test
    public void testInitializerPGinet() throws Exception {
        IPTarget ip = new IPTarget();
        
        Assert.assertEquals("Empty IPTarget must not have a host", null, ip.getHost());
    }

    @Test
    public void testInitializerString() throws Exception {
        IPTarget ip = null;
        try{
            ip = new IPTarget("");

            Assert.fail("An empty String shouldn't be allowed");
        } catch(IllegalArgumentException x) {
            // expected
        }
    }
}
