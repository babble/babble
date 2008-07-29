
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

assert( sysexec( "/bin/ls -la" ).out );
assert( sysexec( "/bin/ls -la" ).err != null );
assert( sysexec( "/bin/ls -la" ).out.match( /\.\./ ) );

assert( sysexec( "/bin/ls" ).out != sysexec( "/bin/ls" , null , null  , "src" ).out );
