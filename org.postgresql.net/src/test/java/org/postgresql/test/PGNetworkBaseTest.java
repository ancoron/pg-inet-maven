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
package org.postgresql.test;

import junit.framework.Assert;
import org.postgresql.net.PGinet;
import junit.framework.TestCase;

/**
 *	Unit tests for the PGNetworkBase.
 *
 *	@author ancoron
 */
public class PGNetworkBaseTest extends TestCase
{
	public PGNetworkBaseTest( String name )
	{
		super( name );
	}

	protected void setUp() throws Exception
	{
	}

	protected void tearDown() throws Exception
	{
	}

    public void testIPv6Bytes() throws Exception
    {
        PGinet inet = new PGinet("2002::7b2d:4340");
        
        Assert.assertEquals("Unexpected value", "2002:0000:0000:0000:0000:0000:7b2d:4340", inet.getValue());
    }
}
