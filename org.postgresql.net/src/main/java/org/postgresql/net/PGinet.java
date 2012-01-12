/*-------------------------------------------------------------------------
*
*	Copyright (C) 2005, PostgreSQL Global Development Group
*
*--------------------------------------------------------------------------
*/
package org.postgresql.net;

import java.io.Serializable;
import java.sql.SQLException;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

/**
 *	This represents org.postgresql's inet datatype, which is used
 *	to hold IPv4 and IPv6 network addresses and ip addresses.
 *
 *	<p>This class wraps the postgresql specific INET datatype.  It supports
 *	values in the following format.</p>
 *
 *	<p>IPv4	a.b.c.d[/netmask]</p>
 *	<p>IPv6 x:x:x:x:x:x:x:x[/netmask]</p>
 *	<p>IPv6 x:x:x:x:x:x:a.b.c.d[/netmask] - IPv4 embedded in IPv6</p> 
 *	<p>The use of :: is also allowed in IPv6 addresses.</p>
 *	<p>See [RFC 2373] and [RFC 2732]
 *	for the complete description of how to specify an IPv6 address.</p>
 *
 *	@author Russell Francis (russ@metro-six.com)
 */
public class PGinet extends PGNetworkBase implements Serializable, Cloneable
{
	/**
 	 *	This constructor takes a string in a valid literal format
	 *	for either an IPv4 or IPv6 address and creates a new PGinet to
	 *	represent it.
	 *
 	 *	@param s The string representation of the inet value.
	 *	@exception SQLException If the string is invalid.
	 */
	public PGinet( String s )
	throws SQLException
	{
		this();
		this.setValue( s );
	}

	public PGinet()
	{
		setType( "inet" );
	}

	/**
	 *	Compare two PGinet's for equality.
	 *
	 *	@param obj The object which we wish to compare.
	 *	@return true if it represents the same network or ip address 
	 *		as this PGinet, false otherwise.
	 */
	public boolean equals( Object obj )
	{
		if( obj instanceof PGinet )
		{
			return( super.equals( obj ) );
		}

		return( false );
	}

	/**
 	 *	Make a duplicate of this PGinet object.
	 *
	 *	@return null on failure, or a new PGinet address
	 *		which is equal to this object.
	 */
	public Object clone()
	{
		try
		{
			return( new PGinet( this.getValue() ) );
		}
		catch( SQLException e )
		{
			return( null );
		}
	}

	/**
	 *	This method sets the value of this PGinet object.
	 *
 	 *	@param v A string representation of an inet address a.b.c.d[/netmask]
	 *	@exception SQLException If the parameter is not a valid inet address.
	 */
	public void setValue( String v )
	throws SQLException
	{
		if( v == null )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}

		String host = this.getHostPortion( v );

		if( isIPv6Address( host ) )
		{
			this.netmask = this.getNetmaskPortion( v, 128 );
			if( ( this.netmask < 0 ) || ( this.netmask > 128 ) )
			{
				this.netmask = 0;
				this.addr = null;
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
					new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
			}
		}
		else if( isIPv4Address( host, false ) )	// don't allow shortened ipv4 notation.
		{
			this.netmask = this.getNetmaskPortion( v, 32 );
			if( ( this.netmask < 0 ) || ( this.netmask > 32 ) )
			{
				this.netmask = 0;
				this.addr = null;
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
					new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
			}
		}
		else
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}
	}
}
