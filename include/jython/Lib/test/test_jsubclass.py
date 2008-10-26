from test_support import *

print 'Subclassing Java from Python (test_jsubclass.py)'

from java.lang import Runnable, Thread

# Overriding Methods

print 'override methods'
class MyThread(Thread):
        count = 0
        def run(self):
                self.count = self.count+1


t1 = MyThread()
t1.start()
t1.join()
assert t1.count == 1, 'subclassing java.lang.Thread'

print 'pass subclass back to java'

class MyRun(Runnable):
        count = 0
        def run(self):
                self.count = self.count+1

run = MyRun()
t = Thread(run)
t.start()
t.join()
assert run.count == 1, 'subclassing java.lang.Thread'

print "invoke super's constructor"

class MyThread(Thread):
        def __init__(self):
                self.name = "Python-"+self.name

t = MyThread()
assert t.name[:14] == "Python-Thread-", 'automatic constructor call'

class MyThread(Thread):
        def __init__(self):
                Thread.__init__(self, "Python-Thread")

t = MyThread()
assert t.name == "Python-Thread", 'explicit constructor call'

