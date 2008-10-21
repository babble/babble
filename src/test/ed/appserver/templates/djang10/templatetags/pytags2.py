import _10gen

register = _10gen.djang10.Library()

def pyfilter2(value, arg):
    return "pyfilter2:%s%s"%(value[::-1], arg[::-1])

register.filter("pyfilter2", pyfilter2)