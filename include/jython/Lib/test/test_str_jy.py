from test import test_support
import unittest

class WrappedStrCmpTest(unittest.TestCase):

    def testWrappedWorksAsKey(self):
        '''Test for http://jython.org/bugs/1816134

        PyString's equal used to check for str explicitly, so Wrapper's __cmp__ wasn't used
        and a KeyError would be raised by the lookup on ABC.
        '''
        class Wrapper(object):
            def __init__(self, content):
                self.content = content
            def __hash__(self):
                return hash(self.content)
            def __cmp__(self, other):
                if isinstance(other, Wrapper):
                    return cmp(self.content, other.content)
                return cmp(self.content, other)
        d = {'ABC' : 1}
        ABC = Wrapper('ABC')
        self.assertEquals(1, d[ABC])

class IntToStrTest(unittest.TestCase):

    def test_int_to_string_format(self):
        # 0.001 comes out as 0.0010
        self.assertEquals(str(0.001), "0.001")

class StringSlicingTest(unittest.TestCase):

    def test_out_of_bounds(self):
        try:
            "a"[10:]
        except StringOutOfBoundsError:
            self.fail("str slice threw StringOutOfBoundsError")

class FormatTest(unittest.TestCase):

    def test_add_zeros(self):
        # 2 "%012d" % -4 displays '0000000000-4'
        s = "%012d" % -4
        self.assertEquals(s, "-00000000004")

    def test_format(self):
        "%#.0f, %e and %+f w/ negative numbers print correctly."
        self.assertEquals("%.1f" % 5, "5.0")
        self.assertEquals("%e" % -1e-6, "-1.000000e-06")
        self.assertEquals("%e" % 0, "0.000000e+00")
        self.assertEquals("%e" % 1e-6, "1.000000e-06")
        self.assertEquals("%+f" % -5, "-5.000000")
        self.assertEquals("%+f" % 5, "+5.000000")


    def test_argument_count_exception(self):
        "exception thrown when too many or too few arguments for format string"
        foo = False
        try:
            r = '%d' % (1, 2)
        except TypeError, e:
            self.failUnless("not all arguments converted" in str(e))

        try:
            r = '%d%d' % 1
        except TypeError, e:
            self.failUnless("not enough arguments for format string" in str(e))
        try:
            s = '%d%d' % (1,)
        except TypeError, e:
            self.failUnless("not enough arguments for format string" in str(e))

    def test_unicode_arg(self):
        # When the right-side operand is a unicode, the result should be unicode
        # too
        self.assertEquals("%s" % u"foo", u"foo")
        self.assertEquals("%s" % u"\u00e7", u"\u00e7")

    def test_unicode_in_args(self):
        # When at least one of the right-side operands is a unicode, the result
        # should be unicode too
        self.assertEquals("%s %s" % (u"foo", "bar"), u"foo bar")
        self.assertEquals("%s %s" % ("foo", u"bar"), u"foo bar")

        class S(object):
            def __str__(self): return "str"
            def __unicode__(self): return "unicode"

        # Also, once a unicode has been found, next args should be __unicode__'d
        self.assertEquals("%s %s %s" % ("foo", u"bar", S()), u"foo bar unicode")
        # But, args found before the first unicode should not be __unicode__'d
        self.assertEquals("%s %s %s" % (S(), u"bar", S()), u"str bar unicode")

    def test_non_ascii_unicode_mod_str(self):
        # Regression test for a problem on the formatting logic: when no unicode
        # args were found, Jython stored the resulting buffer on a PyString,
        # decoding it later to make a PyUnicode. That crashed when the left side
        # of % was a unicode containing non-ascii chars
        self.assertEquals(u"\u00e7%s" % "foo", u"\u00e7foo")


class DisplayTest(unittest.TestCase):

    def test_str_and_repr(self):
        class s(str):
            pass
        class u(str):
            pass

        for cls in str, s, unicode, u:
            foo = cls('foo')
            for expr in 'str(foo)', 'foo.__str__()':
                result = eval(expr)
                self.assert_(type(result) == str)
                self.assertEqual(result, 'foo')

            for expr in 'repr(foo)', 'foo.__repr__()':
                result = eval(expr)
                self.assert_(type(result) == str)
                if issubclass(cls, unicode):
                    self.assertEqual(result, "u'foo'")
                else:
                    self.assertEqual(result, "'foo'")

    def test_basic_escapes(self):
        test = '\r\n\tfoo\a\b\f\v'
        self.assertEqual(repr(test), "'\\r\\n\\tfoo\\x07\\x08\\x0c\\x0b'")
        self.assertEqual(repr(unicode(test)), "u'\\r\\n\\tfoo\\x07\\x08\\x0c\\x0b'")
        test2 = "'bar"
        self.assertEqual(repr(test2), '"\'bar"')
        self.assertEqual(repr(unicode(test2)), 'u"\'bar"')

def test_main():
    test_support.run_unittest(WrappedStrCmpTest,
        IntToStrTest,
        StringSlicingTest,
        FormatTest,
        DisplayTest)

if __name__ == '__main__':
    test_main()
