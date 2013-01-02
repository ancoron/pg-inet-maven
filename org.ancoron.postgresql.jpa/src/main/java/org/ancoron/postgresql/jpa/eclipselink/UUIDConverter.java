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
package org.ancoron.postgresql.jpa.eclipselink;

import java.util.UUID;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

/**
 * Supports mapping of <tt>java.util.UUID</tt> inside JPA entities.
 * 
 * <p>
 * Example usage:
 * <pre>
 * // ...
 * import java.util.UUID;
 * import org.ancoron.postgresql.jpa.eclipselink.UUIDConverter;
 * // ...
 *  
 * &#064;Entity
 * &#064;Converter(name="uuidConverter",converterClass=UUIDConverter.class)
 * public class Elink implements Serializable {
 *     &#064;Convert("uuidConverter")
 *     &#064;Column(name="elink_id")
 *     private UUID elinkId;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *   
 * @author Edward Mann
 * 
 * @see http://dev.eclipse.org/mhonarc/lists/eclipselink-users/msg07359.html
 */
public class UUIDConverter implements Converter {

    @Override
    public Object convertObjectValueToDataValue(Object objectValue,
            Session session) {
        return objectValue;
    }

    @Override
    public UUID convertDataValueToObjectValue(Object dataValue,
            Session session) {
        return (UUID) dataValue;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field;
        if (mapping instanceof DirectCollectionMapping) {
            // handle @ElementCollection...
            field = ((DirectCollectionMapping) mapping).getDirectField();
        } else {
            field = mapping.getField();
        }

        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("uuid");
        field.setColumnDefinition("UUID");
    }
}
