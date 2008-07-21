
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

A = function(){
    this.thingy = 2;
};
A.prototype.junk = 45;
A.prototype.word = 4;

a = new A();
print(a.junk);
print(a.thingy);
print(a.word);
print( a.prototype ? "yes" : "no" );

print( "---" );

B = function(){
    this.thingy = 1;
    this.zzz = "zzz";
};

B.prototype = new A();
//B.prototype.constructor = B;
B.prototype.junk = 54;

b = new B();
print(b.junk);
print(b.thingy);
print(b.word);
print(b.zzz);

print( "---" );

a = new A();
print(a.junk);
print(a.thingy);
print(a.word);
