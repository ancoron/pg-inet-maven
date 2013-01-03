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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import javax.management.RuntimeErrorException;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.descriptors.MethodAttributeAccessor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.DirectMapMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.ManyToOneMapping;
import org.eclipse.persistence.mappings.ObjectReferenceMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.converters.SerializedObjectConverter;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventListener;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;

/**
 *
 * @author ancoron
 */
public class ConverterInitializer implements SessionEventListener {

    protected final Set<ObjectReferenceMapping> references = new HashSet<ObjectReferenceMapping>();

    @Override
    public void missingDescriptor(SessionEvent event) {
        // no-op
    }

    @Override
    public void moreRowsDetected(SessionEvent event) {
        // no-op
    }

    @Override
    public void noRowsModified(SessionEvent event) {
        // no-op
    }

    @Override
    public void outputParametersDetected(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireExclusiveConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postBeginTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCalculateUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCalculateUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCommitTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCommitUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postConnect(SessionEvent event) {
        // no-op
    }

    @Override
    public void postExecuteQuery(SessionEvent event) {
        // no-op
    }

    @Override
    public void postReleaseClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void postReleaseUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postResumeUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postRollbackTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void postDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void preBeginTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCommitTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCommitUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preExecuteQuery(SessionEvent event) {
        // no-op
    }

    @Override
    public void prepareUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseExclusiveConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preRollbackTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void preMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    private Class getFieldType(final Class c, final String attributeName, final boolean mapValueType) {
        for(Field f : c.getDeclaredFields()) {
            if(f.getName().equals(attributeName)) {
                return extractType(f.getGenericType(), f.getType(), mapValueType);
            }
        }

        return null;
    }

    private Class getMethodReturnType(final Class c, final String methodName, final boolean mapValueType) {
        for(Method m : c.getDeclaredMethods()) {
            if(m.getName().equals(methodName)) {
                return extractType(m.getGenericReturnType(),
                        m.getReturnType(), mapValueType);
            }
        }

        return null;
    }

    private Class getMethodParameterType(final Class c, final String methodName, final boolean mapValueType) {
        for(Method m : c.getDeclaredMethods()) {
            if(m.getName().equals(methodName)) {
                return extractType(m.getGenericParameterTypes()[0],
                        m.getParameterTypes()[0], mapValueType);
            }
        }

        return null;
    }

    private Class extractType(Type gt, Class type, final boolean mapValueType) {
        if(gt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) gt;
            if(Collection.class.isAssignableFrom(type)
                    || Map.class.isAssignableFrom(type)) {
                // a list, set or map...
                Type[] args = pt.getActualTypeArguments();
                if(args.length > 1 && mapValueType) {
                    // must be a map...
                    return (Class) args[1];
                } else {
                    return (Class) args[0];
                }
            } else {
                // use outer type...
                return type;
            }
        } else {
            return type;
        }
    }

    protected Converter getConverter(final Class c, final String attributeName,
            final String methodName, final boolean mapValueType)
    {
        Converter conv = null;
        Class s = c;
        Class type = null;

        // also traverse up the super-classes...
        do {
            if(methodName == null) {
                // field access...
                type = getFieldType(s, attributeName, mapValueType);
            } else if(methodName.startsWith("set")) {
                // setter access...
                type = getMethodParameterType(s, methodName, mapValueType);
            } else {
                // getter access...
                type = getMethodReturnType(s, methodName, mapValueType);
            }
        } while(type == null && (s = s.getSuperclass()) != null);

        if(type != null) {
            // set converters as appropriate...
            if(IPNetwork.class.isAssignableFrom(type)) {
                conv = new IPNetworkConverter();
            } else if(IPTarget.class.isAssignableFrom(type)) {
                conv = new IPTargetConverter();
            } else if(InetAddress.class.isAssignableFrom(type)) {
                conv = new InetAddressConverter();
            } else if(PGcidr.class.isAssignableFrom(type)) {
                conv = new PGcidrConverter();
            } else if(PGinet.class.isAssignableFrom(type)) {
                conv = new PGinetConverter();
            } else if(PGmacaddr.class.isAssignableFrom(type)) {
                conv = new PGmacaddrConverter();
            } else if(UUID.class.isAssignableFrom(type)) {
                conv = new UUIDConverter();
            } else {
                conv = null;
            }
        }

        return conv;
    }

    @Override
    public void preLogin(SessionEvent event) {
		// Iterate over the descriptors
		// Identify DirectToFieldMappings for our special types
		// 	Fix converter if required
		// 	Set field type to java.sql.Types.OTHER

        Session s = event.getSession();
        s.getSessionLog().log(4, "Fixing database descriptor mappings...");
        
		Map<Class, ClassDescriptor> descriptorMap = s.getDescriptors();

		for (Map.Entry<Class, ClassDescriptor> entry : descriptorMap.entrySet()) {
            Class cls = entry.getKey();
            ClassDescriptor desc = entry.getValue();
			Vector<DatabaseMapping> mappings = desc.getMappings();
			for (DatabaseMapping mapping : mappings) {

                DirectCollectionMapping dcm = null;
                DirectToFieldMapping dfm = null;
                DirectMapMapping dmm = null;
                Converter conv = null;
                Converter conv2 = null;
                String attribute = mapping.getAttributeName();
                String method = null;
                DatabaseField f = null;
                DatabaseField f2 = null;

                if (mapping.getAttributeAccessor() instanceof MethodAttributeAccessor) {
                    MethodAttributeAccessor maa = (MethodAttributeAccessor) mapping.getAttributeAccessor();
                    if(!maa.isWriteOnly()) {
                        method = maa.getGetMethodName();
                    } else {
                        method = maa.getSetMethodName();
                    }
                }
                
                if (mapping instanceof DirectToFieldMapping) {
					dfm = (DirectToFieldMapping) mapping;
                    conv = dfm.getConverter();
                    f = dfm.getField();
                } else if (mapping instanceof DirectMapMapping) {
					dmm = (DirectMapMapping) mapping;
                    conv = dmm.getKeyConverter();
                    conv2 = dmm.getValueConverter();
                    f = dmm.getDirectKeyField();
                    f2 = dmm.getDirectField();
                } else if(mapping instanceof DirectCollectionMapping) {
                    dcm = (DirectCollectionMapping) mapping;
                    conv = dcm.getValueConverter();
                    f = dcm.getDirectField();
                } else if (mapping instanceof ObjectReferenceMapping) {
                    references.add((ObjectReferenceMapping) mapping);
                    // post-pone references...
                    continue;
                }

                // only consider mappings that are deemed to produce
                // byte[] database fields from objects...
                if(conv != null && conv instanceof SerializedObjectConverter) {
                    conv = getConverter(cls, attribute, method, false);
				}

                if(conv2 != null && conv2 instanceof SerializedObjectConverter) {
                    conv2 = getConverter(cls, attribute, method, true);
                }

                if(conv != null) {
                    s.getSessionLog().log(4, "Using converter "
                            + conv.getClass().getName()
                            + " for field " + f.getTableName()
                            + "." + f.getName());

                    if(dfm != null) {
                        dfm.setConverter(conv);
                    } else if(dcm != null) {
                        dcm.setValueConverter(conv);
                    } else {
                        dmm.setKeyConverter(conv);
                    }
                }

                if(conv2 != null) {
                    s.getSessionLog().log(4, "Using converter "
                            + conv2.getClass().getName()
                            + " for field " + f2.getTableName()
                            + "." + f2.getName());

                    dmm.setValueConverter(conv2);
                }
			}
		}
    }

    @Override
    public void postLogin(SessionEvent event) {
        for(Iterator<ObjectReferenceMapping> it = references.iterator(); it.hasNext(); ) {
            ObjectReferenceMapping mapping = it.next();

            try {
                if(mapping instanceof OneToOneMapping) {
                    OneToOneMapping m = (OneToOneMapping) mapping;
                    Map<DatabaseField, DatabaseField> fMap = m.getSourceToTargetKeyFields();
                    for(Map.Entry<DatabaseField, DatabaseField> fEntry : fMap.entrySet()) {
                        DatabaseField source = fEntry.getKey();
                        DatabaseField target = fEntry.getValue();

                        if(DatabaseField.NULL_SQL_TYPE == source.getSqlType()
                                && DatabaseField.NULL_SQL_TYPE != target.getSqlType())
                        {
                            // set column type to avoid varchar default...
                            source.setSqlType(target.getSqlType());
                        }
                    }
                }
            } catch(RuntimeException x) {
                // log errors, but don't fail the login...
                event.getSession().getSessionLog().logThrowable(6, x);
            } finally {
                // always remove the entry...
                it.remove();
            }
        }
    }

    public void preLogout(SessionEvent se) {
        // no-op
    }

    public void postLogout(SessionEvent se) {
        // no-op
    }
}
