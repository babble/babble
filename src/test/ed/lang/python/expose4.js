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

myClass = function(){
    this.myAttr = 214;
};

myClass.prototype.foo = "2312";

local.src.test.ed.lang.python.module3();

assert( pythonGetInstanceAttr( myClass, "foo" ) == myClass.prototype.foo );

assert( pythonGetInstanceAttr( myClass, "myAttr" ) == 214 );

assert( pythonGetClassAttr( myClass, "foo" ) == myClass.prototype.foo );

assert( pythonGetClassAttr( myClass, "bind" ) == Function.prototype.bind );

pythonExtend( myClass );

var c1 = new myClass();

assert( c1.pyList[2] == -3 );

assert( c1.pyMeth(" W. 20th") == "2312 W. 20th" );

assert( myClass.pyMeth( c1 , "31" ) == "231231" );

assert( myClass.pyMeth.call( c1 , "-14=2298" ) == "2312-14=2298" );

// just make sure these don't blow up horribly; no idea what they should 
// actually return
pyStr.keySet();
pyCallable.keySet();
pyObject.keySet();

jsFoo = {a: 1, b: 2, f: 3, "c#": "j"};

pythonKeys(jsFoo);
