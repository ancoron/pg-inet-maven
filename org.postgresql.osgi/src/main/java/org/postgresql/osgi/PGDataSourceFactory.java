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
package org.postgresql.osgi;

import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Constants related to OSGi/JDBC - RFC #122.
 *
 * @author ancoron
 */
public interface PGDataSourceFactory extends DataSourceFactory {

	/**
	 * The "ssl" property (a boolean value).
	 */
	public static final String JDBC_PG_SSL = "ssl";

	/**
	 * The "sslfactory" property (a full class name).
     * 
     * <p>
     * The class specified here must extend the class
     * <code>javax.net.ssl.SSLSocketFactory</code>.
     * </p>
	 */
    public static final String JDBC_PG_SSL_FACTORY = "sslfactory";
    
    public static final String JDBC_PG_COMPATIBLE = "compatible";

	/**
	 * The "loginTimeout" property (an integer value).
	 */
    public static final String JDBC_PG_LOGIN_TIMEOUT_IN_SECONDS = "loginTimeout";

	/**
	 * The "prepareThreshold" property (an integer value).
     * 
     * <p>
     * This specifies the number of times a statement object must be reused
     * before server-side prepare is enabled.
     * </p>
	 */
    public static final String JDBC_PG_STATEMENT_PREPARE_THRESHOLD = "prepareThreshold";

	/**
	 * The "socketTimeout" property (an integer value).
	 */
    public static final String JDBC_PG_SOCKET_TIMEOUT_IN_SECONDS = "socketTimeout";

	/**
	 * The "tcpkeepalive" property (a boolean value).
	 */
    public static final String JDBC_PG_TCP_KEEPALIVE = "tcpkeepalive";

	/**
	 * The "unknownLength" property (an integer value).
	 */
    public static final String JDBC_PG_UNKNOWN_LENGTH = "unknownLength";
}
