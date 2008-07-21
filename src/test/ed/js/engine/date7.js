
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

var d = new Date( 999999999 );
d.setMonth(0);
d.setDate(3);
d.setYear(1984); 

print( d );
print( "Z" + d );
print( 5 + d );

var d2 = new Date( 999999999 );
d2.setMonth(0);
d2.setDate(6); // Friday; lexically less than Tuesday
d2.setYear(1984);

var s = "" + d;
var s2 = "" + d2;

print(d < d2);
print(s < s2);
