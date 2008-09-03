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
_10gen.assert.eq( _10gen.jsString , "some string" )

_10gen.assert.eq( _10gen.jsString.jsAttr , 42 )

pyS = _10gen.jsStringProcess("a new string");

_10gen.assert.eq( pyS.someAttr , 13 );

pyS = ''

_10gen.assert.raises( lambda: pyS.foo )

def setAttr(pyS):
    pyS.foo = 144

_10gen.assert.raises( lambda: setAttr(pyS) )

_10gen.assert.eq( " string" , _10gen.jsString[4:] )
_10gen.assert.eq( 0 , _10gen.jsString.find( 's' ) )
_10gen.assert.eq( "s" , _10gen.jsString[5] )
_10gen.assert.raises( lambda: _10gen.jsString[102] )
