_orig = object

import sys
try:
    sys.builtins['object'] = 0
except (NameError, TypeError), e:
    # NameError because sys.builtins doesn't exist; TypeError because it's r/o
    pass
assert not isinstance(object, int), 'sys.builtins got modified'

try:
    sys.builtins = {'object': 1}
except TypeError, e:
    pass

assert not isinstance(object, int), 'sys.builtins got replaced'

import __builtin__
try:
    __builtin__.__dict__ = {'object': 2}
except:
    pass
assert not isinstance(object, int), 'builtin.__dict__ got replaced'

try:
    __builtin__.__dict__['object'] = 3
except:
    pass
assert not isinstance(object, int), 'builtin.__dict__ got modified'

# This import should be blocked, but even if it works, this should be r/o
try:
    import org.python.core.PySystemState
    org.python.core.PySystemState.builtins['object'] = 4
except:
    pass

assert not isinstance(object, int), 'java import got modified'

# Even if it's r/o, it could be replaced -- I hope the import is blocked
try:
    org.python.core.PySystemState.builtins = {'object': 5}
except:
    pass

assert not isinstance(object, int), 'java import got replaced'


