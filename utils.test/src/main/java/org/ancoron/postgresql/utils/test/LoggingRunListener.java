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
package org.ancoron.postgresql.utils.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 * @author ancoron
 */
public class LoggingRunListener extends RunListener {
    
    private static final Logger log = Logger.getLogger("test.logger");
    
    private static Set<String> classes = new HashSet<String>();
    private static Map<String, Long> startTimes = new HashMap<String, Long>();

    @Override
    public void testFinished(Description d) throws Exception {
        double end = (double) System.currentTimeMillis();
        double start = startTimes.get(d.getDisplayName()).doubleValue();
        log.log(Level.INFO, "Finished ({1} ms): {0}", new Object[] {
            d.getMethodName(),
            String.format("%.3f", ((end - start) / 1000D))
        });
    }

    @Override
    public void testFailure(Failure f) throws Exception {
        log.log(Level.WARNING, "Failed: {0}", new Object[] {
            f.getDescription().getDisplayName(),
        });
    }

    
    @Override
    public void testStarted(Description d) throws Exception {
        if(!classes.contains(d.getClassName())) {
            classes.add(d.getClassName());
            log.log(Level.FINE, "Starting test class: {0}", d.getClassName());
        }

        log.log(Level.FINE, "Starting: {0}", d.getMethodName());
        
        startTimes.put(d.getDisplayName(), System.currentTimeMillis());
    }
    
}
