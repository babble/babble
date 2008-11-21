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

try:
    import google.atom.service
except ImportError, e:
    # This is OK -- can't import from outside a site with a _config packages map
    pass
else:
    raise AssertionError

import import3_help
for globals in [{}, None]:
    for locals in [{}, None]:
        for fromlist in [[]]: # could try None here too
            m = __import__('import3_help', globals, locals, fromlist)
            assert m == import3_help
