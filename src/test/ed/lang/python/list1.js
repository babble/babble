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

local.src.test.ed.lang.python.listPython();

var pyL = getList();

assert( pyL.length == 2 );

assert( pyL.some( function(x){ return x == 1; } ) );
assert( pyL.every( function(x){ return x > 0; } ) );

pyL.push(8);

assert( pyGetLength(pyL) == 3 );
assert( pyCheckEven(pyL, 2) );

pyL.push(9);

var i = 0;
for( key in pyL ){
    assert( key == i , 'iterating is broken -- #'+i+' key is '+ key );
    assert.eq( pyL[ key ] , pyL[ i ] );
    ++i;
}
assert( i == pyL.length , 'wrong number of keys; ' + i + ' found, should be ' + pyL.length );

assert( ! pyCheckEven(pyL, 3) );

var jsA = [1, 3, 5];

assert( pyManipList(jsA) );

newL = pyL.filter(function(x){ return x % 2;});
assert( newL[0] == 1 );
assert( newL[1] == 9 );
assert( newL.length == 2 );

var flip = true;
newL = pyL.filter(function(){ flip = !flip; return flip; });
assert( newL[0] == pyL[1] );
assert( newL.length == pyL.length/2 );
