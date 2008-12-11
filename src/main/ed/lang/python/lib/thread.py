import _10gen
from org.python.modules.thread.thread import LockType, __doc__, allocate_lock, \
    exit, exit_thread, _local, error, get_ident

def start_new_thread(func, args):
    _10gen.__instance__.queueWork( _generate_name(), func, *args )

_newFunctionThread = start_new_thread

# interruptAllThreads is not exported

def stack_size():
    raise NotImplemented, 'stack_size not supported for 10gen threads'

_number = 0
_number_mutex = allocate_lock()
def _generate_name():
    global _number
    _number_mutex.acquire()
    name = 'python-thread-%d'%_number
    _number += 1
    _number_mutex.release()
    return name

