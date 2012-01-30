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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author ancoron
 */
public class SingleLineLogFormatter extends Formatter
{
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = System.getProperty("line.separator");
    
    protected Date dat = new Date();
    protected boolean printClass = true;
    protected boolean printMethod = true;
    protected boolean printLevel = true;

    @Override
    public synchronized String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();
        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        sb.append("[").append(df.format(dat)).append("]");
        
        if(printClass || printMethod) {
            sb.append("[");
        }

        if(printClass) {
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());
            } else {
                sb.append(record.getLoggerName());
            }
        }

        if(printMethod) {
            if (record.getSourceMethodName() != null) {
                if(printClass) {
                    sb.append("::");
                }

                sb.append(record.getSourceMethodName());
            }
        }

        if(printClass || printMethod) {
            sb.append("] ");
        }

        sb.append(String.format("%7s", record.getLevel().getLocalizedName()));

        String message = formatMessage(record);
        sb.append(": ");
        sb.append(message);
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }
}
