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

local.src.test.ed.lang.python.size1_helper();

// These get converted to immutable objects.

//print("boolean " + pyBoolean.approxSize());
//print("int " + pyInt.approxSize());

//print("short string " + pyString.approxSize());
//print("longer string " + pyString2.approxSize());

var start = pyDict2.approxSize();

var before = pyDict2.approxSize();
//assert.eq(start, before); // FIXME

pyModifyDict2();

assert.eq(pyDict2.approxSize() , before);


