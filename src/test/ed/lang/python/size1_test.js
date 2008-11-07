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

var before = pyDict2.approxSize(); // str1: str2
assert.eq( before , pyDict2.approxSize() );
assert( before >= start );
assert( before - start < 100 );
assert.eq( before , pyDict2.approxSize() );


pyModifyDict2(); // str1: str2
assert.eq(pyDict2.approxSize() , before);

pyModifyDict3(); // str1: str2, str2: str2
assert(pyDict2.approxSize() > before);

var extraNode = pyDict2.approxSize();

var delta = extraNode - before; // cost of adding an entry to a hash

pyModifyDict4(); // str1: str2, str2: str1

assert.eq(extraNode, pyDict2.approxSize());


var start = pyList1.approxSize();
var before = pyList1.approxSize();
assert.eq( before , pyList1.approxSize() );
assert( before >= start );
assert( before - start < 100 );

var diff = before - pyList2.approxSize();
assert(diff > 0); // same object reused [1,1]

assert.eq(before , pyList3.approxSize());

var before = pyList1.approxSize();

pyList1.push(1);
pyList2.push(1);
var delta = pyList1.approxSize() - before; // cost of an extra node
assert( delta > 0 );
assert.eq(pyList1.approxSize() - pyList2.approxSize() , diff); // added to both
