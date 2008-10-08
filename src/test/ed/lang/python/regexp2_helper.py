'''
    Copyright (C) 2008 10gen Inc.

    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
'''

import _10gen

assert(_10gen.re1.search("a"))
assert(_10gen.re1.search("abba"))
assert(not _10gen.re1.search("b"))
assert(not _10gen.re1.search("Abba"))

assert(_10gen.re2.search("a"))
assert(_10gen.re1.search("abba"))
assert(not _10gen.re2.search("b"))
assert(_10gen.re2.search("Abba"))

assert(_10gen.re3.search("a"))
assert(_10gen.re3.search("abba"))
assert(not _10gen.re3.search("b"))
assert(_10gen.re3.search("Abba"))

assert(_10gen.re4.search("bb"))
assert(_10gen.re4.search("bab"))
assert(_10gen.re4.search("baab"))
assert(_10gen.re4.search("baaab"))
assert(not _10gen.re4.search("baaaab"))

assert(_10gen.re5.search("").group(1) == "")
assert(_10gen.re5.search("/").group(1) == "")
assert(_10gen.re5.search("aa").group(1) == "aa")
assert(_10gen.re5.search("aoeu").group(1) == "aoeu")
assert(_10gen.re5.search("/hello/world/").group(1) == "hello/world/")
