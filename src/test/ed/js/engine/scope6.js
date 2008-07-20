
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

option = { a : "1" };
opt = 1;

function(t) {
    for( opt in option ) {
        print( opt );
    }
};

print( opt );


increm = function(options){
    var n = options.n;
    return function(i){
        return i+n;
    };
};

f = increm({n: 5});
print(f(2));

g = increm({n: 8});
print(g(2));

print(f(2));
