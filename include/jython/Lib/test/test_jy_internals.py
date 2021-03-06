"""
 test some jython internals
"""
import unittest
import time
from test.test_support import run_suite

import java
import jarray

from org.python.core import Py
from java.sql import Date, Time, Timestamp
import datetime


class WeakIdentityMapTests(unittest.TestCase):

    def test_functionality(self):
        from org.python.core import IdImpl

        i = java.lang.Integer(2)
        j = java.lang.Integer(2)
        # sanity check
        assert i == j and i is not j
        h = java.lang.Integer(2)

        widmap = IdImpl.WeakIdentityMap()
        widmap.put(i,'i')
        widmap.put(j,'j')
        widmap.put(h,'h')

        assert widmap.get(i) == 'i'
        assert widmap.get(j) == 'j'
        assert widmap.get(h) == 'h'
        # white box double-check
        assert widmap._internal_map_size() == 3

        widmap.remove(h)
        assert widmap.get(h) is None
        # white box double-check
        assert widmap._internal_map_size() == 2

        # white box test for weak referencing of keys
        del j
        java.lang.System.gc()
        java.lang.System.runFinalization()
        java.lang.System.gc()
        java.lang.System.runFinalization()
        time.sleep(1)

        assert widmap.get(i) == 'i' # triggers stale weak refs cleanup
        assert widmap._internal_map_size() == 1

class LongAsScaledDoubleValueTests(unittest.TestCase):

    def setUp(self):
        self.v = v = 2**53-1 # full mag bits of a double value
        self.v8 = v * 8      # fills up 7 bytes
        self.e = jarray.zeros(1,'i')
        iarr = java.lang.Object.getClass(self.e)
        sdv = java.lang.Class.getMethod(long, 'scaledDoubleValue', [iarr])
        import org.python.core.PyReflectedFunction as ReflFunc
        self.sdv = ReflFunc(sdv)

    def test_basic_roundtrip(self):
        e = self.e
        sdv = self.sdv
        assert long(sdv(0L, e)) == 0
        assert e[0] == 0
        assert long(sdv(1L, e)) == 1
        assert e[0] == 0
        assert long(sdv(-1L, e)) == -1
        assert e[0] == 0
        assert long(sdv(self.v, e)) == self.v
        assert e[0] == 0

    def test_scale_3_v(self):
        e = self.e
        v = self.v8
        sdv = self.sdv
        assert long(sdv(v, e)) == v
        assert e[0] == 0
        assert long(sdv(v+1, e)) - v == 0
        assert e[0] == 0

    def test_no_worse_than_doubleValue(self):
        e = self.e
        v = self.v8
        sdv = self.sdv
        for d in range(8):
            assert float(v+d) == sdv(v+d, e)
            assert e[0] == 0

        for d in range(8):
            for y in [0,255]:
                assert float((v+d)*256+y) == sdv((v+d)*256+y, e)
                assert e[0] == 0

        for d in range(8):
          for y in [0,255]:
            assert float((v+d)*256+y) == sdv(((v+d)*256+y)*256, e)
            assert e[0] == 1

class ExtraMathTests(unittest.TestCase):
    def test_epsilon(self):
        from org.python.core.util import ExtraMath
        self.assertNotEqual(1.0 + ExtraMath.EPSILON, 1.0)
        self.assertEqual(1.0 + (ExtraMath.EPSILON/2.0), 1.0)
    def test_close(self):
        from org.python.core.util import ExtraMath
        self.assert_(ExtraMath.close(3.0, 3.0))
        self.assert_(ExtraMath.close(3.0, 3.0 + ExtraMath.CLOSE))
        self.assert_(not ExtraMath.close(3.0, 3.0 + 4.0*ExtraMath.CLOSE))
    def test_closeFloor(self):
        from org.python.core.util import ExtraMath
        import math
        self.assertEquals(ExtraMath.closeFloor(3.5), 3.0)
        self.assertEquals(ExtraMath.closeFloor(3.0 - ExtraMath.EPSILON), 3.0)
        self.assertEquals(
          ExtraMath.closeFloor(3.0 - 3.0 * ExtraMath.CLOSE), 2.0)
        self.assertEquals(ExtraMath.closeFloor(math.log10(10**3)), 3.0)

class DatetimeTypeMappingTest(unittest.TestCase):
    def test_date(self):
        self.assertEquals(datetime.date(2008, 5, 29),
                          Py.newDate(Date(108, 4, 29)))
        self.assertEquals(datetime.date(1999, 1, 1),
                          Py.newDate(Date(99, 0, 1)))

    def test_time(self):
        self.assertEquals(datetime.time(0, 0, 0),
                          Py.newTime(Time(0, 0, 0)))
        self.assertEquals(datetime.time(23, 59, 59),
                          Py.newTime(Time(23, 59, 59)))

    def test_datetime(self):
        self.assertEquals(datetime.datetime(2008, 1, 1),
                          Py.newDatetime(Timestamp(108, 0, 1, 0, 0, 0, 0)))
        self.assertEquals(datetime.datetime(2008, 5, 29, 16, 50, 0),
                          Py.newDatetime(Timestamp(108, 4, 29, 16, 50, 0, 0)))
        self.assertEquals(datetime.datetime(2008, 5, 29, 16, 50, 1, 1),
                          Py.newDatetime(Timestamp(108, 4, 29, 16, 50, 1, 1000)))


def test_main():
    test_suite = unittest.TestSuite()
    test_loader = unittest.TestLoader()
    def suite_add(case):
        test_suite.addTest(test_loader.loadTestsFromTestCase(case))
    suite_add(WeakIdentityMapTests)
    suite_add(LongAsScaledDoubleValueTests)
    suite_add(ExtraMathTests)
    suite_add(DatetimeTypeMappingTest)
    run_suite(test_suite)

if __name__ == "__main__":
    test_main()

