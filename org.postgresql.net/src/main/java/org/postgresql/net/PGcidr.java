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
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

/**
 *	This represents org.postgresql's cidr datatype, which is
 *	used to hold both IPV4 &amp; IPV6 network addresses.
 *
 *	@author Russell Francis (russ@metro-six.com)
 */
public class PGcidr extends PGNetworkBase implements Serializable, Cloneable
{
	/**
	 *	This method will accept an IPv4 or IPv6 network address
	 *	in any of the following formats.
	 *
	 *	<p><ul>
	 *	<li>A full IPv6 address [RFC 2373] or [RFC 2732] optionally
	 *		followed by a netmask in the range of 0 to 128.</li>
	 *	<li>A partial or full IPv4 address, optionally followed by a 
	 *		netmask. a[.b[.c[.d]]][/netmask].</li></ul></p>
	 *
	 *	@param s The representation of the cidr as a string.
	 *	@exception SQLException if the string is not in the proper format.
	 */
	public PGcidr( String s )
	throws SQLException
	{
		this();
		setValue( s );
	}

	public PGcidr()
	{
		setType( "cidr" );
	}

	/**
	 *	Compare two PGcidr objects for equality.
	 *
	 *	<p>This will return true if the parameter obj is of type PGcidr
	 *	and represents the same network as this.
	 *
	 *	@param obj The object which we wish to compare.
	 *	@return true if it represents the same network as this, false otherwise.
	 */
	public boolean equals( Object obj )
	{
		if( obj instanceof PGcidr )
		{
			return( super.equals( obj ) );
		}
		return( false );
	}

	/**
	 *	This will make a duplicate of the current PGcidr object.
	 *
	 *	@return null on failure, or a new PGcidr object which
	 *		represents the same network address as the invoking
	 *		object.
	 */
	public Object clone()
	{
		try
		{
			return( new PGcidr( this.getValue() ) );
		}
		catch( SQLException e )
		{
			return( null );
		}
	}

	/**
	 *	Set the value of this CIDR.
	 *
	 *	<p>For IPv4 network addresses, this method will accept strings in the
	 *	following format a[.b[.c[.d]]][/netmask] where a, b, c, d are
	 *	integers in the range of 0 to 255 and netmask can be an integer
	 *	from 0 to 32.
	 *
	 *	<p>IPv6 networks must be entered as a complete IP address as 
	 *	defined in [RFC 2373] or [RFC 2732] optionally followed by 
	 *	a netmask which can be in the range of 0 - 128.
 	 *
	 *	<p>For either IPv4 or IPv6, it is illegal for the host portion
	 *	of the network address to contain anything but zero values.
	 *
 	 *	@param v The string representation of this network address.
	 *	@exception SQLException If it is not in a valid cidr format.
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
			this.netmask = this.getMinimalNetmask( v );
			if( ( this.netmask < 0 ) || ( this.netmask > 128 ) )
			{
				this.netmask = 0;
				this.addr = null;
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
					new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
			}
		}
		else if( isIPv4Address( host, true ) )	// allow shortened ipv4 notation
		{
			this.netmask = this.getMinimalNetmask( v );
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

		ensureHostBitsAreZero();
	}

	/**
	 *	This method will get the netmask for a given address.
	 *
	 *	@param v is the full address passed to setValue we will extract the netmask
	 *		if it exists, if not, we will return a good guess.
	 */
	protected int getMinimalNetmask( String v )
	throws PSQLException
	{
		PGtokenizer t = new PGtokenizer( v, '/' );
		int size = t.getSize();
		if( ( size != 1 ) && ( size != 2 ) )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) ); 
		}

		int mask;

		if( size == 2 )
		{
			// If a netmask is specified, read and use it.
			try
			{
				// we have a netmask to read
				mask = Integer.valueOf( t.getToken( 1 ) ).intValue();
			}
			catch( NumberFormatException e )
			{
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
					new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH, e ) ); 
			}
		}
		else
		{
 			mask = this.addr.length * 8;
			if( mask == 32 )	// IPv4, IPv6 addresses use 128 if none is specified.
			{
				// If no netmask is specified, we use the smallest netmask that
				// will include all of the non-zero bits of the address in 8 bit
				// blocks.
				int a = this.addr[0] & 0xFF; // cast byte to int and force range 0 - 255.
				int b = this.addr[1] & 0xFF;
				int c = this.addr[2] & 0xFF;
				int d = this.addr[3] & 0xFF;
				if( ( a >= 0 ) && ( a <= 127 ) )
				{
					if( b == 0 && c == 0 && d == 0 )
					{
						mask = 8;
					}
					else if( c == 0 && d == 0 )
					{
						mask = 16;
					}
					else if( d == 0 )
					{
						mask = 24;
					}
				}
				else if( ( a >= 128 ) && ( a <= 191 ) )
				{
					if( c == 0 && d == 0 )
					{
						mask = 16;
					}
					else if( d == 0 )
					{
						mask = 24;
					}
				}
				else if( ( a >= 192 ) && ( a <= 223 ) )
				{
					if( d == 0 )
					{
						mask = 24;
					}
				}
			}
		}

		return( mask );
	}

	/**
	 *	A given IP address and netmask specified with a cidr cannot have any
	 *	bits which specify the host as non-zero within the address.
	 *
	 *	<p>This method ensures that this constraint is met.</p>
	 *
	 *	<p>If the constraint is not met, an exception is thrown.</p>
	 */
	protected void ensureHostBitsAreZero()
	throws PSQLException
	{
		// this represents the number of full bytes in the network portion
		// of the address.
		int network_bytes = this.netmask / 8 ;

		// This represents the number of bits in the network portion
		// of the address within the boundary byte, 
		// if the netmask is 24, this is 0
		// if the netmask is 23, this is 7
		// if the netmask is 25, this is 1 ...
		int network_bits = this.netmask % 8;

		// Test the boundary byte to ensure no bits are set in the host
		// portion.
		if( network_bytes < this.addr.length )
		{
			if( ( this.addr[network_bytes] & ( 0xFF >> network_bits ) ) != 0 )
			{
				// bits to the right of the network portion of the 
				// address, this is not allowed.
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: " +
					"host bits not all zero.",
					new Object[]{ type } ), PSQLState.DATA_TYPE_MISMATCH ) ); 
			}
		}

		// test each byte after the boundary byte and ensure that they are zero.
		for( int i = network_bytes + 1; i < this.addr.length; ++i )
		{
			if( this.addr[i] != 0 )
			{
				// bits to the right of the network portion of the 
				// address, this is not allowed.
				throw( new PSQLException( GT.tr( "Conversion to type {0} failed: " +
					"host bits not all zero.",
					new Object[]{ type } ), PSQLState.DATA_TYPE_MISMATCH ) ); 
			}
		}
	}
}
