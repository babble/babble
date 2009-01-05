import _10gen
import ed.appserver.AppContext
import ed.lang.python.Python
import java.io.File

# FIXME: this test produces a lot of output, due to prints in the
# queueWork calls
_10gen.__instance__ = ed.lang.python.Python.toPython(ed.appserver.AppContext(java.io.File('.')))
_10gen.log.level = _10gen.log.LEVEL.ERROR

import test.test_thread
import test.test_threading
