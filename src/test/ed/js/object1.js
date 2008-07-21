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

a = Object.extend({}, {foo: 1, bar: 2});
assert(a.foo == 1);
assert(a.bar == 2);

b = Object.extend(a, {baz: 8});

assert(b.foo == 1);
assert(b.bar == 2);
assert(b.baz == 8);
assert(a.baz == 8);

c = Object.extend(a, {foo: 5});

assert(c.foo == 5);
assert(a.foo == 5);
assert(c.bar == 2);

d = Object.extend("hi", {newfunc: function(){ return this[0]; } });
assert(d.newfunc() == 'h');

e = "yo";
assert(e.newfunc == null);

a = {a: 1, b: 2, c: 3};
vals = Object.values(a);
for(var n = 1; n <= 3; n++){
    i = vals.indexOf(n);
    assert(i != -1);
    vals.splice(i, 1);
}

assert(vals.length == 0);

a = {a: 1, b: 2, c: 3};
keys = Object.keys(a);
realKeys = ["a", "b", "c"];
for(var n = 0; n <= 2; n++){
    i = keys.indexOf(realKeys[n]);
    assert(i != -1);
    keys.splice(i, 1);
}

assert(keys.length == 0);
