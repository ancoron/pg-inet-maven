/*-------------------------------------------------------------------------
*
*	Copyright (C) 2005, PostgreSQL Global Development Group
*
*--------------------------------------------------------------------------
*/
package org.postgresql.test.net;

import org.postgresql.test.TestUtil;
import org.postgresql.net.PGmacaddr;
import junit.framework.TestCase;
import java.sql.*;
import java.net.*;

/**
 *	Unit tests for the PGmacaddr data type.
 *
 *	@author Russell Francis (russ@metro-six.com)
 */
public class PGmacaddrTest extends TestCase
{
	private Connection dbConn;
	private static final String tableName = "testpgmacaddr";

	public PGmacaddrTest( String name )
	{
		super( name );
	}

	protected void setUp() throws Exception
	{
		dbConn = TestUtil.openDB();
		TestUtil.createTable( dbConn, this.tableName, "mac macaddr" );
	}

	protected void tearDown() throws Exception
	{
		TestUtil.dropTable( dbConn, this.tableName );
	}

	/**
 	 * 	This method will test that the PGmacaddr type refuses to create
	 *	objects when passed invalid mac addresses.
	 */
	public void testPGmacaddrInvalid() 
	throws SQLException
	{
		String[] invalidMacAddresses = {
			"ab:ab:ab:ab:ab:ab:ab",		// to many pairs.
			"ab:abc:ab:ab:ab:ab",		// pair two is wrong length.
			"ab:ab-ab:ab-ab:ab",		// not all the same delimiter
			"fg:ab:cd:ef:01:23",		// invalid hex character g
			":af:af:af:af:af:af",		// starts with a ':'
			"12:34:56:78:9a:bc:"		// ends with a ':'
		};

		// Test that we can create networks 
		for( int i = 0; i < invalidMacAddresses.length; ++i )
		{	
			PGmacaddr address = this.makePGmacaddr( invalidMacAddresses[i] );
			assertNull( "An invalid mac address was turned into a PGmacaddr object: " + 
				invalidMacAddresses[i] + "' failed.", address );
		}
	}

	/**
 	 * 	This method will test that the PGmacaddr type creates
	 *	PGmacaddr objects when passed valid mac addresses.
 	 *	
	 *	<p>It will also ensure that they can be inserted into Postgres
	 *	and the they can be read from Postgres and that they maintain
	 *	equality after the ordeal.</p>
	 */
	public void testPGmacaddrValid() throws SQLException
	{
		// Each address should be unique in the list or this test
		// will fail.
		String[] validMacAddresses = {
			"12:34:56:65:43:21",
			"123457:754321",
			"abcdee-eedcba",
			"ab-cd-ef-fe-dc-ba",
			"1234.5678.9abc"
		};

		// Insert each of the networks into the table.
		PreparedStatement s = dbConn.prepareStatement( TestUtil.insertSQL( tableName, "?" ) );
		for( int i = 0;i < validMacAddresses.length; ++i )
		{
			PGmacaddr address = this.makePGmacaddr( validMacAddresses[i] );
			assertNotNull( "A valid mac address was unable to be converted into a PGmacaddr object: '" +
				validMacAddresses[i] + "' failed.", address );

			s.setObject( 1, address );
			assertEquals( 1, s.executeUpdate() );
		}

		s = dbConn.prepareStatement( "SELECT * FROM " + tableName + " WHERE mac = ?" );
		// Retrieve each of the networks from the table.
		for( int i = 0; i < validMacAddresses.length; ++i )
		{
			PGmacaddr address = this.makePGmacaddr( validMacAddresses[i] );
			assertNotNull( "A valid address was unable to be converted into a PGmacaddr object: '" +
				validMacAddresses[i] + "' failed.", address );

			s.setObject( 1, address );
			ResultSet rs = 	s.executeQuery();
			int count = 0;
			while( rs.next() )
			{
				PGmacaddr fetchedAddress = (PGmacaddr)rs.getObject( 1 );
				assertNotNull( "Unable to fetch inserted mac address from the database: '" + 
					validMacAddresses[i] + "' failed.", address );

				assertEquals( "The retrieved address and the inserted address are not equal!",
					fetchedAddress, address );

				assertEquals( "The retrieved address and the inserted address have " +
					"different hashCodes!", fetchedAddress.hashCode(), address.hashCode() );

				++count;
			}

			assertEquals( "Selected more than 1 row!", 1, count );
		}
	}

	/**
	 *	This is a local utility method which will take a string
	 *	and attempt to create a new PGmacaddr object.
	 *
	 *	<p>If the object cannot be created, null will be returned.</p>
	 *
	 *	@param value The textual representation of a macaddress.
	 *	@return null on failure or a PGmacaddr object which represents
	 *		the value parameter.
	 */
	private PGmacaddr makePGmacaddr( String value )
	{
		try
		{
			return( new PGmacaddr( value ) );
		}
		catch( Exception e )
		{
			return( null );
		}
	}
}
