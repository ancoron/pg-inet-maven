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
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

/**
 *	This represents org.postgresql's macaddr datatype, which is used
 *	to store NIC mac addresses.
 *
 *  <p>There are 5 possible valid forms which we will accept
 *  for a mac address which is a sextuple of hex digits.</p>
 *
 *	<p>	
 *	<ul>
 *	<li>xxxx.xxxx.xxxx</li>
 *	<li>xxxxxx:xxxxxx</li>
 *	<li>xx:xx:xx:xx:xx:xx</li>
 *	<li>xx-xx-xx-xx-xx-xx</li>
 *	<li>xxxxxx-xxxxxx</li>
 *	</ul>
 *	</p>
 *
 *	@author Russell Francis (russ@metro-six.com)
 */	
public class PGmacaddr extends PGobject implements Serializable, Cloneable
{
	byte[] macaddress = null;
	Integer hashCode = null;
	String strMac = null;

	/**
	 *	Construct a new mac address from a string in one of the formats
	 *	specified above.
	 *
	 *	@param s A macaddress represented as a string.
	 *	@exception SQLException This is thrown if the string is not a 
	 *		valid macaddress.
	 */
	public PGmacaddr( String s )
	throws SQLException
	{
		this();
		this.setValue( s );
	}

	public PGmacaddr()
	{
		setType( "macaddr" );
	}

	/**
	 *	This method will get an array of bytes which make up the mac address.
	 *
	 *	@return An array of bytes which make up the mac address or null, if
	 *		no address has been assigned.
	 */
	public byte[] getBytes()
	{
		return( this.macaddress );
	}

	/**
	 *	Set the value of the mac address.
	 *
	 *	There are 5 possible valid forms which we will accept
	 *	for a mac address which is a sextuple of hex digits
	 *
	 *	1.	xxxx.xxxx.xxxx
	 *	2.	xxxxxx:xxxxxx
	 *	3. 	xx:xx:xx:xx:xx:xx
	 *	4.	xx-xx-xx-xx-xx-xx
	 *	5.	xxxxxx-xxxxxx
	 *
	 *	@param v A mac address in one of the formats listed above.
	 */
	public void setValue( String v )
	throws SQLException
	{
		if( v == null )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}

		if( v.startsWith( ":" ) || v.startsWith( "." ) || v.startsWith( "-" ) ||
			v.endsWith( ":" ) || v.endsWith( "." ) || v.endsWith( "-" ) )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}

		String strippedMacAddr = null;

		// this is the format we output macaddr's in
		// so we will check it first.
		// the case xx:xx:xx:xx:xx:xx  or xxxxxx:xxxxxx
		PGtokenizer t = new PGtokenizer( v, ':' );
		if( ( t.getSize() == 6 ) || ( t.getSize() == 2 ) )
		{
			strippedMacAddr = strip( v, ':' ); 
		}

		if( strippedMacAddr == null )
		{
			// lets try case xxxxxx-xxxxxx
			// or xx-xx-xx-xx-xx-xx
			t = new PGtokenizer( v, '-' );
			if( ( t.getSize() == 2 ) || ( t.getSize() == 6 ) )
			{
				strippedMacAddr = strip( v, '-' );
			}
		}

		if( strippedMacAddr == null )
		{
			// try the case where they are separated by dots.
			// xxxx.xxxx.xxxx
			t = new PGtokenizer( v, '.' );
			if( t.getSize() == 3 )
			{
				strippedMacAddr = strip( v, '.' );
			}
		}

		// If we don't have a stripped mac address then we fail here.
		if( strippedMacAddr == null )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );	
		}

		// There should be 6 pairs of hexadecimal digits.  This
		// will make any valid macaddress 12 characters long.
		if( strippedMacAddr.length() != 12 )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}

		byte[] mac;
		try
		{
			mac = new byte[ 6 ];
			for( int i = 0; i < 6; ++i )
			{
				int twoi = i * 2;
				mac[i] = (byte)(Integer.valueOf( 
					strippedMacAddr.substring( twoi, twoi + 2 ), 16 ).intValue() & 0xFF);
			}
		}
		catch( NumberFormatException e )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) );
		}

		this.macaddress = mac;
		this.hashCode = null;
		this.strMac = null;
	}

	/**
 	 *	This will compare two mac addresses for equality.
	 *
	 *	@param obj Another PGmacaddr object to compare with this one.
	 *	@return true if they are equivalent, false if not.
	 */
	public boolean equals( Object obj )
	{
		if( obj instanceof PGmacaddr )
		{
			PGmacaddr addr = (PGmacaddr)obj;
			byte[] thatBytes = addr.getBytes();
			if( ( this.macaddress == null ) || ( thatBytes == null ) )
			{
				return( false );
			}

			if( thatBytes.length == this.macaddress.length )
			{
				if( this.macaddress.length == 6 )
				{
					for( int i = 0; i < 6; ++i )
					{
						if( thatBytes[i] != this.macaddress[i] )
						{
							return( false );
						}
					}
					return( true );
				}
			}
		}
		return( false );
	}

	/**
	 *	Get a hash code for this mac address.
	 *
	 *	@return a hash value for this object.
	 */
	public int hashCode()
	{
		if( this.hashCode == null )
		{
			int hashValue = this.macaddress[4] | (this.macaddress[5] << 8) | 
				(this.macaddress[4] << 16) | (this.macaddress[5] << 24);
			for( int i = 0; i < 4; ++i )
			{
				hashValue ^= this.macaddress[i] << (i << 3);
			}
			this.hashCode = new Integer( hashValue );
		}

		return( this.hashCode.intValue() );
	}

	/**
	 *	This method will print the value of this mac address as a string.
	 *
	 *	@return A string representation of this mac address in the
	 *		xx:xx:xx:xx:xx:xx format.
	 */
	public String getValue()
	{
		if( this.strMac == null )
		{
			StringBuffer val = new StringBuffer();
	
			for( int i = 0; i < this.macaddress.length; ++i )
			{
				if( i > 0 )
				{
					val.append( ":" );
				}
				val.append( Integer.toHexString( ((int)this.macaddress[i]) & 0xFF ) );
			}
			this.strMac = val.toString();
		}

		return( this.strMac );
	}

	/**
	 *	This method will remove all occurences of char
	 *	from a string.
	 *
	 *	@param s The string to strip.
	 *	@param ch The character to strip from s.
	 *	@return A new string without any occurences of
	 *		ch.
	 */
	private String strip( String s, char ch )
	{
		StringBuffer str = new StringBuffer();
		for( int i = 0; i < s.length(); ++i )
		{
			char c = s.charAt( i );
			if( c != ch )
			{
				str.append( c );
			}
		}
		return( str.toString() );
	}
}
