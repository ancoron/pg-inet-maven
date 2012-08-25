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
package org.ancoron.postgresql.jpa.eclipselink;

import java.sql.SQLException;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;
import org.postgresql.util.PGobject;

/**
 * Supports mapping of <tt>org.postgresql.net.PGmacaddr</tt> inside JPA entities.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * // ...
 * import org.postgresql.net.PGmacaddr;
 * import org.ancoron.postgresql.jpa.eclipselink.PGmacaddrConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="pgmacaddrConverter", converterClass=PGMacaddrConverter.class)
 * public class PGmacaddrEntity implements Serializable {
 *     
 * 
 *     &#064;Convert("pgmacaddrConverter")
 *     &#064;Column(name="c_mac")
 *     private PGmacaddr mac;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author ancoron
 * 
 * @see PGmacaddr
 */
public class PGmacaddrConverter implements Converter {

    @Override
    public PGmacaddr convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return null;
        } else if (objectValue instanceof PGmacaddr) {
            return (PGmacaddr) objectValue;
        } else if (objectValue instanceof String) {
            try {
                return new PGmacaddr((String) objectValue);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert object value of type "
                + objectValue.getClass().getName() + " into a "
                + PGmacaddr.class.getName());
    }

    @Override
    public PGmacaddr convertDataValueToObjectValue(Object dataValue, Session session) {
        if (dataValue == null) {
            return null;
        } else if (dataValue instanceof PGmacaddr) {
            return (PGmacaddr) dataValue;
        } else if (dataValue instanceof PGobject) {
            try {
                return new PGmacaddr(((PGobject) dataValue).getValue());
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert data value of type "
                + dataValue.getClass().getName() + " into a "
                + PGinet.class.getName());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field;
        if(mapping instanceof DirectCollectionMapping) {
            // handle @ElementCollection...
            field = ((DirectCollectionMapping) mapping).getDirectField();
        } else {
            field = mapping.getField();
        }

        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("macaddr");
        field.setColumnDefinition("MACADDR");
    }
}
