import thread  # should this actually be safe?
import sys
import _10gen

def foo():
    print "can't happen"

_10gen.assert.throws( lambda: thread.start_new_thread(foo, ()) )


del sys.modules['thread']
import thread

_10gen.assert.throws( lambda: thread.start_new_thread(foo, ()) )

