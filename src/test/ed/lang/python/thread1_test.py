import sys
import _10gen

def import_thread():
    import thread
    print "can't happen"

def import_threading():
    import threading
    print "really can't happen"

def get_thread():
    return sys.modules['thread']

def get_threading():
    return sys.modules['threading']

_10gen.assert.throws( lambda: import_thread() )
_10gen.assert.throws( lambda: import_threading() )
_10gen.assert.throws( lambda: get_thread() )
_10gen.assert.throws( lambda: get_threading() )

if 'thread' in sys.modules:
    del sys.modules['thread']

_10gen.assert.throws( lambda: import_thread() )
_10gen.assert.throws( lambda: import_threading() )
_10gen.assert.throws( lambda: get_thread() )
_10gen.assert.throws( lambda: get_threading() )


