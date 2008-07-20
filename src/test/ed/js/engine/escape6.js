// escape6.js

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

print( unescape( "%" ) );

print( unescape( "a%!!b" ) );
print( unescape( "a%5!b" ) );
print( unescape( "a%51b" ) );

print( encodeURIComponent( "anasdas12425234542353245225345" ) );
print( encodeURIComponent( " " ) );
print( encodeURIComponent( "%" ) );
print( encodeURIComponent( "@" ) );
print( encodeURIComponent( "`~!@#$%^&*()-_=+" ) );
print( encodeURIComponent( "[]{}\|;:''\"\"/?><.," ) );

print( "!!!" );

print( escape( "anasdas12425234542353245225345" ) );
print( escape( " " ) );
print( escape( "%" ) );
print( escape( "@" ) );
print( escape( "`~!@#$%^&*()-_=+" ) );
print( escape( "[]{}\|;:''\"\"/?><.," ) );

print( "!!!" );

print( encodeURI( "anasdas12425234542353245225345" ) );
print( encodeURI( " " ) );
print( encodeURI( "%" ) );
print( encodeURI( "@" ) );
print( encodeURI( "`~!@#$%^&*()-_=+" ) );
print( encodeURI( "[]{}\|;:''\"\"/?><.," ) );


print( unescape( escape ( "anasdas12425234542353245225345" ) ) );
print( unescape( escape ( " " ) ) );
print( unescape( escape ( "%" ) ) );
print( unescape( escape ( "@" ) ) );
print( unescape( escape ( "`~!@#$%^&*()-_=+" ) ) );
print( unescape( escape ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );

print( decodeURI( encodeURI ( "anasdas12425234542353245225345" ) ) );
print( decodeURI( encodeURI ( " " ) ) );
print( decodeURI( encodeURI ( "%" ) ) );
print( decodeURI( encodeURI ( "@" ) ) );
print( decodeURI( encodeURI ( "`~!@#$%^&*()-_=+" ) ) );
print( decodeURI( encodeURI ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );


print( decodeURIComponent( encodeURIComponent ( "anasdas12425234542353245225345" ) ) );
print( decodeURIComponent( encodeURIComponent ( " " ) ) );
print( decodeURIComponent( encodeURIComponent ( "%" ) ) );
print( decodeURIComponent( encodeURIComponent ( "@" ) ) );
print( decodeURIComponent( encodeURIComponent ( "`~!@#$%^&*()-_=+" ) ) );
print( decodeURIComponent( encodeURIComponent ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );
