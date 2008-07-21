
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

a = 5;
print( a instanceof Number );

a = null;
print( a instanceof Object );

b = null;
a = [ 1 , 2];

print( a instanceof Object );
print( a instanceof Array );

print( ( new Date()) instanceof Date );



A = function(){

};

a = new A();

print( a instanceof A );


newClass = function(){
    klass = function(){

    };
    return klass;
};

c1 = newClass();
c2 = newClass();

c1obj = new c1();

print(c1obj instanceof c1);
print(c1obj instanceof c2);
