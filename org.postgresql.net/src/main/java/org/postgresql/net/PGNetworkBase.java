package org.postgresql.net;

import java.io.Serializable;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
class PGNetworkBase extends PGobject implements Serializable, Cloneable
{
	protected byte[] addr;
	protected int netmask;
	protected boolean embedded_ipv4 = false;

	protected String addrString = null;
	protected Integer hashCode = null;

	/**
	 *	This will read an IPv4 address string in to the instance
	 *	variable addr.
	 *
	 *	<p>If there is a syntax error in the format of the host string
	 *	false will be returned.
	 *
	 *	@param host A host parameter in IPv4 dotted quad notation.
	 *	@param allowShortNotation Whether we should accept addresses
	 *		in the [a[.b[.c[.d]]] format or a.b.c.d format.
	 *	@return true if this is an IPv4 host, false if not.
	 *		If true is returned, this.addr will contain the valid
	 *		bytes which make up this IPv4 address.
	 */
	protected boolean isIPv4Address( String host, boolean allowShortNotation )
	{
		if( host == null )
		{
			return( false );
		}

		PGtokenizer t = new PGtokenizer( host, '.' );
		int size = t.getSize();
		if( 
			( ( allowShortNotation == false ) && ( size != 4 ) ) ||
			( ( allowShortNotation == true ) && ( size < 1 || size > 4 ) ) )
		{
			return( false );
		}

		// create space for the new ipv4 address and zero it out.
		byte[] address = new byte[ 4 ];

		try
		{
			for( int i = 0; i < size; ++i )
			{
				String chunk = t.getToken( i );
				if( chunk.length() == 0 )
				{
					return( false );
				}

				int v = Integer.parseInt( chunk );
				if( ( v < 0 ) || ( v > 255 ) )
				{
					return( false );
				}

				address[i] = (byte)(v & 0xFF);
			}
		}
		catch( NumberFormatException e )
		{
			return( false );
		}

		// this is an ipv4 address.
		this.addr = address;
		this.hashCode = null;
		this.addrString = null;
		return( true );
	}

	/**
	 *	This method will return true if this is a valid syntactically correct
	 *	IPv6 address.  It will also fill in the byte array member variable
	 *	with the 16 byte values which make up the address.
	 *
	 *	@param host The host portion of the address with the netmask stripped off.
	 *	@return true if this is a valid IPv6 address, false if not.
	 */
	protected boolean isIPv6Address( String host )
	{
		// Lets get some easy checks out of the way.
		if( host == null )
		{
			return( false );
		}

		// All IPv6 Addresses contain ':'
		int index = host.indexOf( ":" );
		if( index == -1 )
		{
			return( false );
		}

		// Lets see if we use the shorthand legally '::' can be used once and 
		// only once.
		int shorthandIndex = host.indexOf( "::", index );
		if( shorthandIndex != -1 )
		{
			// Ok, we use the IPv6 shorthand notation at least once.
			// Lets look for another usage or ":::" which isn't valid.
			if( host.indexOf( "::", shorthandIndex + 1 ) != -1 )
			{
				// we either have ":::" or multiple uses of the shorthand,
				// which isn't allowed.
				return( false );
			}
		}

		// ensure that if we start with a ':' there are two of them
		// ensure that if we end with a ':', again, there are two of them.
		if(	( host.endsWith( ":" ) && !host.endsWith( "::" ) )	||
			( host.startsWith( ":" ) && !host.startsWith( "::" ) ) )
		{
			return( false );
		}
		
		//
		//	0 1  2 3  4 5   1011 1213 1415 
		//	XXXX:XXXX:XXXX::YYYY:YYYY:YYYY
		//
		// i = 5, j = 10, n = 15
		//
		// If the host uses shorthand notation, we will split host around "::"
		// We will use the front-end (head) to fill elements of 0 -> i of address.
		// We will use the tail-end (tail) to fill the elements j -> n on address.
		//

		String head = host;
		String tail = "";
		if( shorthandIndex != -1 )
		{
			head = host.substring( 0, shorthandIndex );
			tail = host.substring( shorthandIndex + 2 );
		}

		// Ok, the host has passed all of our tests sofar, lets parse
		// the host into tokens.
		PGtokenizer headTokenizer = new PGtokenizer( head, ':' );
		int headSize = headTokenizer.getSize();
		PGtokenizer tailTokenizer = new PGtokenizer( tail, ':' );
		int tailSize = tailTokenizer.getSize();

		// Lets allocate space for this potential ipv6 address.
		byte[] address = new byte[ 16 ];

		try
		{
			// Process the head.
			int i = 0;
			int k = 0;
			for(;k < headSize; ++k )
			{
				String chunk = headTokenizer.getToken( k );
				if( chunk.length() > 0 )
				{
					// if i == 12, we may have an ipv4 address embedded as the last
					// 32 bits of the address.
					if( ( i == 12 ) && ( chunk.indexOf( '.' ) != -1 ) )
					{
						if( isIPv4Address( chunk, false ) )
						{
							System.arraycopy( this.addr, 0, address, 12, 4 );
							this.addr = null;
							this.embedded_ipv4 = true;
						}
					}
					else
					{
						if( i > 14 )
							return( false );
						int chunkValue = Integer.parseInt( chunk, 16 );
						address[ i++ ] = (byte)((chunkValue >> 8) & 0xFF );
						address[ i++ ] = (byte)(chunkValue & 0xFF);
					}
				}
			}

			// Process the tail.
			int j = 15;
			k = 0;
			for(;k < tailSize; ++k )
			{
				String chunk = tailTokenizer.getToken( k );
				if( chunk.length() > 0 )
				{
					if( ( j == 15 ) && ( chunk.indexOf( '.' ) != -1 ) )
					{
						if( isIPv4Address( chunk, false ) )
						{
							System.arraycopy( this.addr, 0, address, 12, 4 );
							this.addr = null;
							this.embedded_ipv4 = true;
							j = 11;
						}
					}
					else
					{
						if( j < 1 )
							return( false );
						int chunkValue = Integer.parseInt( chunk, 16 );
						address[ j-- ] = (byte)(chunkValue & 0xFF );
						address[ j-- ] = (byte)((chunkValue >> 8) & 0xFF );
					}
				}
			}

			if( i - j > 1 )
			{
				// Too many chunks in the host address
				return( false );
			}
		}
		catch( NumberFormatException e )
		{
			return( false );
		}

		this.addr = address;
		this.hashCode = null;
		this.addrString = null;
		return( true );
	}

	/**
	 *	This parameter takes a string representation of an inet address with
	 *	optional netmask and returns the host portion without the netmask
	 *	as a string.
	 *
	 *	@param v The full network address with optional /netmask. addr/netmask
	 *	@return The addr portion of the string with the optional netmask removed.
	 *	@exception PSQLException This is thrown if the string v is not a valid
	 *		network address.
	 */
	protected String getHostPortion( String v )
	throws PSQLException
	{
		PGtokenizer t = new PGtokenizer( v, '/' );

		int size = t.getSize();
		if( ( size != 1 ) && ( size != 2 ) )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) ); 
		}

		return( t.getToken( 0 ) );
	}

	/**
	 */
	protected int getNetmaskPortion( String v, int def  )
	throws PSQLException
	{
		PGtokenizer t = new PGtokenizer( v, '/' );

		int size = t.getSize();
		if( ( size != 1 ) && ( size != 2 ) )
		{
			throw( new PSQLException( GT.tr( "Conversion to type {0} failed: {1}.",
				new Object[]{ type, v } ), PSQLState.DATA_TYPE_MISMATCH ) ); 
		}

		int mask = def;

		if( size == 2 )
		{
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

		return( mask );
	}

	/**
	 *	Get the hash code for this network address.
	 *
 	 *	@return The hash value for this object.
	 */
	public int hashCode()
	{
		if( this.hashCode == null )
		{
			// Seems like it will generate a decent hash?
			int hashValue = this.netmask | (~this.netmask << 8) | ( this.netmask << 16) << (~this.netmask << 24);
			for( int i = 0; i < this.addr.length; ++i )
			{
				hashValue ^= this.addr[i] << ((i % 4) << 3);
			}
			this.hashCode = new Integer( hashValue );
		}

		return( this.hashCode.intValue() );
	}

	/**
	 *	Returns the inet address in literal format.
	 *
	 *	@return A string value of the inet address in literal format.
	 */
	public String getValue()
	{
		if( this.addrString == null )
		{
			StringBuffer s = new StringBuffer();
			boolean isIPv4 = (this.addr.length == 4) ? true : false;
			boolean isIPv6 = (this.addr.length == 16) ? true : false;

			for( int i = 0; i < this.addr.length; ++i )
			{
				if(	isIPv4 || ( ( i >= 12 ) && ( embedded_ipv4 == true ) ) )
				{
					String digit = Integer.toString( ((int)this.addr[i]) & 0xFF );
					s.append( digit );
					if( ( isIPv4 && ( i < 3 ) ) || ( isIPv6 && ( i < 15 ) ) )
					{
						s.append( "." );
					}
				}
				else
				{	
					String hexDigit = Integer.toHexString( ((int)this.addr[i]) & 0xFF );
					if( hexDigit.length() == 1 )
					{
						s.append( "0" );
					}
					s.append( hexDigit );
		
					if( ( (i % 2) == 1 ) && ( i != 15 ) )
					{
						s.append( ":" );
					}
				}
			}

			if(	( ( this.addr.length == 16 ) && ( this.netmask < 128 ) ) ||
				( ( this.addr.length == 4 ) && ( this.netmask < 32 ) ) )
			{
				s.append( "/" + this.netmask );
			}
			this.addrString = s.toString();
		}
			
		return( this.addrString );
	}

	/**
	 *	This will return the netmask of the current network address.
	 *
	 *	@return The netmask of the PGinet object.
	 */
	public int getNetmask()
	{
		return( this.netmask );
	}

	public boolean equals( Object obj )
	{
		if( obj instanceof PGNetworkBase )
		{
			PGNetworkBase inet = (PGNetworkBase)obj;
			if( inet.netmask == this.netmask )
			{
				if( inet.addr.length == this.addr.length )
				{
					for( int i = 0; i < this.addr.length; ++i )
					{
						if( inet.addr[ i ] != this.addr[ i ] )
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
}
