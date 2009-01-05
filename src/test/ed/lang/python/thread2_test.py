import _10gen
import ed.appserver.AppContext
import ed.lang.python.Python
import java.io.File

_10gen.__instance__ = ed.lang.python.Python.toPython(ed.appserver.AppContext(java.io.File('.')))

import ed.log.Logger
root = ed.log.Logger.getLogger('python')
old_level = root.get('level')

import ed.log.Level
root.set('level', ed.log.Level.WARN)

import test.test_thread
import test.test_threading

import time
time.sleep(2)
root.set('level', old_level)
