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

print( Math.max( null , null ) );
print( Math.max( 1 , 2 ) );
print( Math.max( 1 , null ) );
print( Math.max( 1 , "a" ) );

print( Math.floor( null ) );
print( Math.floor( 1.2 ) );
print( Math.floor( "a" ) );
print( Math.floor( "1.2" ) );
print( Math.floor( "-1.2" ) );
print( Math.floor( "1" ) );

print( parseInt( "5" ) );
print( parseInt( "-5" ) );
print( parseInt( "asd" ) );
print( parseInt( "ab" , 15 ) );
print( parseInt( "AB" , 15 ) );
print( parseInt( "--5" ) );
print( parseInt( "-A", 12 ) );

print( parseInt( "5.0" ) );
print( parseInt( "5.ueu" ) );
print( parseInt( "5apb" , 16 ) );
print( parseInt( "-54o" , 55 ) );
print( parseInt( "54o" , 55 ) );

print( parseFloat( "-3.1" ) );
print( parseInt( "asd" ) || 5 );

print( Math.abs("3.1") );
print( Math.abs("-3.1") );
print( Math.abs(4.1) );
print( Math.abs(-4.1) );
print( Math.abs("-4") );
print( Math.abs(null) );

print( Math.ceil( 1.2 ) );
print( Math.ceil( "1.2" ) );
print( Math.ceil( "1" ) );
print( Math.ceil( null ) );
print( Math.ceil( Date() ) );
print( Math.ceil( "abb" ) );

print( Math.round( 1.2 ) );
print( Math.round( -2.3 ) );
print( Math.round( 2 ) );
print( Math.round( "44" ) );
print( Math.round( "4.32" ) );
print( Math.round( "0x45" ) );
print( Math.round( "3a" ) );
