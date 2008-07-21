
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

f = function(){
    this.y = true;
};

g = new f();
f.prototype.x = 5;
f.prototype.z = 111;

print(g.__proto__.x);
print(g.__proto__ == f.prototype);

b = function(){
    
};
b.prototype.z = 1;

print( g.__proto__.z );
g.__proto__ = b.prototype;
print( g.__proto__.z );
