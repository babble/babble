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
import random
import types
import _10gen

random.seed()

binary = ''.join([chr(x) for x in random.sample(xrange(256), 256)])
assert isinstance(binary, types.StringType)

_10gen.binary = binary

_10gen.local.src.test.ed.lang.python.binary1_helper()

assert isinstance(_10gen.binary2, types.UnicodeType)
out = _10gen.binary2.encode('ISO-8859-1')

assert binary == out
