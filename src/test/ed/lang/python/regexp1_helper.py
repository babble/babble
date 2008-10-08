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

import re
import _10gen

_10gen.re1 = re.compile("^a")
_10gen.re2 = re.compile("^a", re.IGNORECASE)
_10gen.re3 = re.compile("^a", re.I)
_10gen.re4 = re.compile("ba{0,3}b") # a{,3} doesn't work yet
_10gen.re5 = re.compile("^/?(.*)$")
