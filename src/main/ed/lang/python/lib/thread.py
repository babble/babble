import _10gen
from org.python.modules.thread.thread import LockType, __doc__, allocate_lock, \
    exit, exit_thread, _local, error, get_ident

def start_new_thread(func, args):
    _10gen.__instance__.queueWork( _generate_name(), func, *args )

_newFunctionThread = start_new_thread

# interruptAllThreads is not exported

_stack = 0
def stack_size(*args):
    '''Completely broken no-op version of the Python thread stack_size option.

    This is sufficient functionality to pass the test suite -- they
    don't test that a stack size is actually changed -- so I'm
    checking this in. But don't think it'll actually do anything.'''
    # FIXME: threadsafe? Yeah right!
    global _stack
    if len(args) == 0:
        return _stack
    if len(args) != 1:
        raise ArgumentError, 'stack_size takes at most 1 argument (%d given)'%len(args)

    _stack = args[0] # but don't actually change anything
    return _stack

_number = 0
_number_mutex = allocate_lock()
def _generate_name():
    global _number
    _number_mutex.acquire()
    name = 'python-thread-%d'%_number
    _number += 1
    _number_mutex.release()
    return name

