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

local.src.test.ed.lang.python.regexp1_helper();

assert(re1.test("a"));
assert(re1.test("abba"));
assert(!re1.test("b"));
assert(!re1.test("Abba"));

assert(re2.test("a"));
assert(re2.test("abba"));
assert(!re2.test("b"));
assert(re2.test("Abba"));

assert(re3.test("a"));
assert(re3.test("abba"));
assert(!re3.test("b"));
assert(re3.test("Abba"));

assert(re4.test("bab"));
assert(re4.test("baab"));
assert(re4.test("baaab"));
assert(!re4.test("baaaab"));

assert(re5.exec("")[1] == "");
assert(re5.exec("/")[1] == "");
re5.lastIndex = 0;
assert(re5.exec("aa")[1] == "aa");
re5.lastIndex = 0;
assert(re5.exec("/aoeu")[1] == "aoeu");
re5.lastIndex = 0;
assert(re5.exec("/hello/world/")[1] == "hello/world/");
