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

local.src.test.ed.lang.python.list2_helper();

var newL = pyL2.filter(function(x){
    return x.a == 1;
});

var pyD = newL[0];

assert( pyD.a == 1 );
assert( pyD['a'] == 1 );
assert( pyD.b == 2 );
assert( pyD['b'] == 2 );

var newL2 = pyL2.map(function(x){
    return x.a;
});

assert.eq( newL2.length , 2 );
assert.eq( newL2[0] , 1 );
assert.eq( newL2[1] , null );

var result = pyL2Numbers.reduce(function(x, y){
    return x + y.a;
}, 12);

assert.eq( result, 15 );
