import unittest
from test import test_support
from org.python.tests import Invisible, SubVisible, Visible, VisibleOverride
from org.python.tests import VisibilityResults as Results

class VisibilityTest(unittest.TestCase):
    def test_invisible(self):
        self.assertEquals([], dir(Invisible))

    def test_protected_from_python_subclass(self):
        class SubInvisible(Invisible):
            pass
        # TODO - protectedStaticMethod, protectedStaticField, and protectedField should
        # be here
        s = SubInvisible()
        self.assertEquals(Results.PROTECTED_METHOD, s.protectedMethod(0))

    def test_visible(self):
        self.assertEquals(4, len(dir(Visible)))

        v = Visible()
        self.assertEquals(Results.PUBLIC_FIELD, v.visibleField)
        self.assertEquals(Results.PUBLIC_STATIC_FIELD, Visible.visibleStaticField)
        self.assertEquals(Results.PUBLIC_METHOD, v.visibleInstance(0))
        self.assertEquals(Results.OVERLOADED_PUBLIC_METHOD, v.visibleInstance('a'))
        self.assertEquals(Results.EXTRA_ARG_PUBLIC_METHOD, v.visibleInstance(0, 'b'))
        self.assertEquals(Results.OVERLOADED_EXTRA_ARG_PUBLIC_METHOD,
                v.visibleInstance('a', 'b'))
        self.assertEquals(Results.PUBLIC_STATIC_METHOD, Visible.visibleStatic(0))
        self.assertEquals(Results.OVERLOADED_PUBLIC_STATIC_METHOD, 
                v.visibleStatic('a'))
        self.assertEquals(Results.EXTRA_ARG_PUBLIC_STATIC_METHOD,
                v.visibleStatic(0, 'a'))

        # Ensure that the visibleInstance method from SubVisible that takes a double doesn't
        # leak through to the parent
        self.assertRaises(TypeError, v.visibleInstance, 0.0, 'b')
        # TODO - no way to access a field with the same name as a method
        #self.assertEquals(Results.PUBLIC_METHOD_FIELD, v.visibleInstance)
        #self.assertEquals(Results.PUBLIC_STATIC_METHOD_FIELD, Visible.visibleStatic)

    def test_java_subclass(self):
        s = SubVisible()
        self.assertEquals(Results.PUBLIC_FIELD, s.visibleField)
        self.assertEquals(Results.PUBLIC_STATIC_FIELD, SubVisible.visibleStaticField)
        self.assertEquals(Results.SUBCLASS_STATIC_OVERRIDE, SubVisible.visibleStatic(3))
        self.assertEquals(Results.SUBCLASS_STATIC_OVERLOAD, SubVisible.visibleStatic(3.0, 'a'))
        self.assertEquals(Results.SUBCLASS_OVERRIDE, s.visibleInstance(3))
        self.assertEquals(Results.SUBCLASS_OVERLOAD, s.visibleInstance(3.0, 'a'))
        self.assertEquals(Results.PACKAGE_METHOD, s.packageMethod())
        # Java methods don't allow direct calling of the superclass method, so it should 
        # return the subclass value here.
        self.assertEquals(Results.SUBCLASS_OVERRIDE, Visible.visibleInstance(s, 3))

    def test_in_dict(self):
        for c in Visible, SubVisible, VisibleOverride:
            self.failUnless('visibleInstance' in c.__dict__,
                    'visibleInstance expected in %s __dict__' % c)



def test_main():
    test_support.run_unittest(VisibilityTest)

if __name__ == "__main__":
    test_main()
