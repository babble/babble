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

print( "--- obj " );
var a = {};
print(a.valueOf() === a);
print(typeof a.valueOf());
print(typeof a );

print( "--- date " );

var d = new Date();
print(typeof d.valueOf());
print(typeof d );

print( "--- number " );

var n = 5;
print(typeof n.valueOf());
print(typeof n );

print( "--- string " );

var s = "abcd";
print(typeof s.valueOf());
print(typeof s );

print( "---" );


a = {};

b = {b: "hi"};

a[b] = 5;

print(a[b]);

d = new Date();

a[d] = 1345;
print(a[d]);

for ( foo in a ){
    print( typeof foo );
}
    
