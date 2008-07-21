
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

print( 5 );

var a = 3;
print( a );

a = 4;
print( a );

function foo(){
    return 5;
}

print( foo() );

print( function(){ return 7; }() );

function(){ print( "yay" ); return 5; }
function bar(){ return 5; }

bar();

var hehe = function(){ return 9; };
print( hehe() );
