"Tests for cmp() compatibility with CPython"
import unittest
from test import test_support

class UnicodeDerivedCmp(unittest.TestCase):
    "Test for http://bugs.jython.org/issue1889394"
    def testCompareWithString(self):
        class Test(unicode):
            pass
        test = Test('{1:1}')
        self.assertNotEqual(test, {1:1})
    def testCompareEmptyDerived(self):
        class A(unicode): pass
        class B(unicode): pass
        self.assertEqual(A(), B())

class LongDerivedCmp(unittest.TestCase):
    def testCompareWithString(self):
        class Test(long):
            pass
        self.assertNotEqual(Test(0), 'foo')
        self.assertTrue('foo' in [Test(12), 'foo'])

def test_main():
    test_support.run_unittest(UnicodeDerivedCmp, LongDerivedCmp)

if __name__ == '__main__':
    test_main()
