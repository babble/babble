import _10gen
import ed.appserver.AppContext
import ed.lang.python.Python
import java.io.File

# FIXME: this test produces a lot of output
_10gen.__instance__ = ed.lang.python.Python.toPython(ed.appserver.AppContext(java.io.File('.')))

import test.test_thread
import test.test_threading
