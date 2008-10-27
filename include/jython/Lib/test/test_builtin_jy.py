import sys
import unittest
import test.test_support

class BuiltinTest(unittest.TestCase):
        
    def test_in_sys_modules(self):
        self.assert_("__builtin__" in sys.modules,
            "__builtin__ not found in sys.modules")

    def test_hasattr_swallows_exceptions(self):
        class Foo(object):
            def __getattr__(self, name):
                raise TypeError()
        self.assert_(not hasattr(Foo(), 'bar'))

class LoopTest(unittest.TestCase):

    def test_break(self):
        while 1:
            i = 0
            while i<10:
                i = i+1
            else:
                break

class DebugTest(unittest.TestCase):

    def test_debug(self):
        "__debug__ exists"
        try:
            foo = __debug__
        except NameError, e:
            self.assert_(False)

class GetSliceTest(unittest.TestCase):

    def test_getslice(self):
        class F:
            def __getitem__(self,*args): return '__getitem__ '+`args`
            def __getslice__(self,*args): return '__getslice__ '+`args`
        self.failUnless("__getslice__ (1, 1)" in F()[1:1])

class ChrTest(unittest.TestCase):

    def test_debug(self):
        "chr(None) throws TypeError"
        foo = False
        try:
            chr(None)
        except TypeError, e:
            foo = True
        self.assert_(foo)

class ReturnTest(unittest.TestCase):

    def test_finally(self):
        '''return in finally causes java.lang.VerifyError at compile time'''
        def timeit(f):
            t0 = time.clock()
            try:
                f()
            finally:
                t1 = time.clock()
                return t1 - t0

class ReprTest(unittest.TestCase):
    def test_unbound(self):
        "Unbound methods indicated properly in repr"
        class Foo:
            def bar(s): 
                pass
        self.failUnless(repr(Foo.bar).startswith('<unbound method'))

class CallableTest(unittest.TestCase):

    def test_callable_oldstyle(self):
        class Foo:
            pass
        self.assert_(callable(Foo))
        self.assert_(not callable(Foo()))
        class Bar:
            def __call__(self):
                return None
        self.assert_(callable(Bar()))
        class Baz:
            def __getattr__(self, name):
                return None
        self.assert_(callable(Baz()))

    def test_callable_newstyle(self):
        class Foo(object):
            pass
        self.assert_(callable(Foo))
        self.assert_(not callable(Foo()))
        class Bar(object):
            def __call__(self):
                return None
        self.assert_(callable(Bar()))
        class Baz(object):
            def __getattr__(self, name):
                return None
        self.assert_(not callable(Baz()))

def test_main():
    test.test_support.run_unittest(BuiltinTest,
                                   LoopTest,
                                   DebugTest,
                                   GetSliceTest,
                                   ChrTest,
                                   ReturnTest,
                                   ReprTest,
                                   CallableTest)

if __name__ == "__main__":
    test_main()
