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
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGcidr;
import org.postgresql.util.PGobject;

/**
 * Supports mapping of {@link PGcidr} inside JPA entities.
 * 
 * <p>
 * <strong>Example usage:</strong>
 * 
 * <pre>
 * // ...
 * import org.postgresql.net.PGcidr;
 * import org.ancoron.postgresql.jpa.eclipselink.PGcidrConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="cidrConverter", converterClass=PGcidrConverter.class)
 * public class CIDREntity implements Serializable {
 *     
 *     // ...
 * 
 *     &#064;Convert("cidrConverter")
 *     &#064;Column(name="c_cidr")
 *     private PGcidr cidr;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author ancoron
 * 
 * @see PGcidr
 */
public class PGcidrConverter implements Converter {

    @Override
    public PGcidr convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return null;
        } else if (objectValue instanceof PGcidr) {
            return (PGcidr) objectValue;
        } else if (objectValue instanceof String) {
            try {
                return new PGcidr((String) objectValue);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert object value of type "
                + objectValue.getClass().getName() + " into a "
                + PGcidr.class.getName());
    }

    @Override
    public PGcidr convertDataValueToObjectValue(Object dataValue, Session session) {
        PGcidr net = null;
        if (dataValue == null) {
            return net;
        } else if (dataValue instanceof PGcidr) {
            net = (PGcidr) dataValue;
        } else if (dataValue instanceof PGobject) {
            try {
                // this is a fallback in case special JDBC types are not available...
                net = new PGcidr(((PGobject) dataValue).getValue());
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert data value '"
                        + dataValue + "' into a " + PGcidr.class.getName(), ex);
            }
        }

        if (net == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + PGcidr.class.getName());
        }

        return net;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field = mapping.getField();
        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("cidr");
        field.setColumnDefinition("CIDR");
    }
}
