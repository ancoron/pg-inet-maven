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
package org.ancoron.postgresql.jpa.eclipselink;

import java.sql.SQLException;
import java.util.logging.Logger;
import org.ancoron.postgresql.jpa.IPTarget;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGinet;
import org.postgresql.util.PGobject;

/**
 * Supports mapping of {@link IPTarget} inside JPA entities
 * without having to reference any JDBC driver package explicitely.
 * 
 * <p>
 * <strong>Example usage:</strong>
 * 
 * <pre>
 * // ...
 * import org.ancoron.postgresql.jpa.IPTarget;
 * import org.ancoron.postgresql.jpa.eclipselink.IPTargetConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="ipConverter", converterClass=IPTargetConverter.class)
 * public class IPEntity implements Serializable {
 *     
 *     // ...
 * 
 *     &#064;Convert("ipConverter")
 *     &#064;Column(name="c_ip")
 *     private IPTarget ip;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author ancoron
 * 
 * @see IPTarget
 */
public class IPTargetConverter implements Converter {

    private static final Logger log = Logger.getLogger(IPTargetConverter.class.getName());

    @Override
    public PGinet convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return null;
        } else if (objectValue instanceof IPTarget) {
            return (IPTarget) objectValue;
        } else if (objectValue instanceof PGinet) {
            return (PGinet) objectValue;
        } else if (objectValue instanceof String) {
            try {
                return new PGinet((String) objectValue);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert object value of type "
                + objectValue.getClass().getName() + " into a "
                + PGinet.class.getName());
    }

    @Override
    public IPTarget convertDataValueToObjectValue(Object dataValue, Session session) {
        IPTarget ip = null;
        if (dataValue == null) {
            return ip;
        } else if (dataValue instanceof PGinet) {
            ip = new IPTarget((PGinet) dataValue);
        } else if (dataValue instanceof PGobject) {
            // this is a fallback in case special JDBC types are not available...
            ip = new IPTarget(((PGobject) dataValue).getValue());
        }

        if (ip == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + IPTarget.class.getName());
        }

        return ip;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field = mapping.getField();
        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("inet");
        field.setColumnDefinition("INET");
    }
}
