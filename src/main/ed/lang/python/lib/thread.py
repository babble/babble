import _10gen
from org.python.modules.thread.thread import LockType, __doc__, allocate_lock, \
    exit, exit_thread, _local

def start_new_thread(func, args):
    # FIXME: smarter wrapping in JSPySequenceListWrapper, no need to convert
    # to list
    _10gen.__instance__.queueWork( _generate_name(), func, *args )

_newFunctionThread = start_new_thread

# interruptAllThreads is not exported
def get_ident():
    pass

def stack_size():
    pass

_number = 0
def _generate_name():
    # FIXME: threadsafe
    global _number
    name = 'python-thread-%d'%_number
    _number += 1
    return name

