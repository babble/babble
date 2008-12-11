import _10gen
from org.python.modules.thread.thread import LockType, __doc__, allocate_lock, \
    exit, exit_thread, _local, error

def start_new_thread(func, args):
    # FIXME: smarter wrapping in JSPySequenceListWrapper, no need to convert
    # to list
    _10gen.__instance__.queueWork( _generate_name(), func, *args )

_newFunctionThread = start_new_thread

# interruptAllThreads is not exported
def get_ident():
    raise NotImplemented, 'get_ident not supported for 10gen threads'

def stack_size():
    raise NotImplemented, 'stack_size not supported for 10gen threads'

_number = 0
def _generate_name():
    # FIXME: threadsafe
    global _number
    name = 'python-thread-%d'%_number
    _number += 1
    return name

