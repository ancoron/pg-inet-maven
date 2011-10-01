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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.postgresql.jdbc2.AbstractJdbc2Connection;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
public class ExtendedPostgreSQLPlatform extends PostgreSQLPlatform {

    private static final Logger log = Logger.getLogger(ExtendedPostgreSQLPlatform.class.getName());

    @Override
    public int getJDBCType(Class javaType) {
        if (javaType == PGinet.class || javaType == PGcidr.class || javaType == PGmacaddr.class) {
            return Types.OTHER;
        }
        return super.getJDBCType(javaType);
    }

    @Override
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = super.buildFieldTypes();

        fieldTypeMapping.put(PGinet.class, new FieldTypeDefinition("INET", false));
        fieldTypeMapping.put(PGcidr.class, new FieldTypeDefinition("CIDR", false));
        fieldTypeMapping.put(PGmacaddr.class, new FieldTypeDefinition("MACADDR", false));

        return fieldTypeMapping;
    }

    @Override
    protected Map<String, Class> buildClassTypes() {
        Map<String, Class> classTypeMapping = super.buildClassTypes();

        // Key the Map the other way for table creation.
        classTypeMapping.put("INET", PGinet.class);
        classTypeMapping.put("CIDR", PGcidr.class);
        classTypeMapping.put("MACADDR", PGmacaddr.class);

        return classTypeMapping;
    }

    @Override
    public Connection getConnection(AbstractSession session, Connection connection) {
        final AbstractJdbc2Connection conn;
        if (session.getServerPlatform() != null && (session.getLogin()).shouldUseExternalConnectionPooling()) {
            conn = (AbstractJdbc2Connection) session.getServerPlatform().unwrapConnection(connection);
        } else {
            conn = (AbstractJdbc2Connection) connection;
        }

        try {
            conn.addDataType("CIDR", PGcidr.class);
            conn.addDataType("INET", PGinet.class);
            conn.addDataType("MACADDR", PGmacaddr.class);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        return conn;
    }

    @Override
    protected void initializePlatformOperators() {
        super.initializePlatformOperators();
        
        ExpressionOperator contains = ExpressionOperator.simpleRelation(666, ">>");
        addOperator(contains);
        
        ExpressionOperator containsOrEquals = ExpressionOperator.simpleRelation(667, ">>=");
        addOperator(containsOrEquals);
    }
}
