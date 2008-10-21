import _10gen

register = _10gen.djang10.Library()

def pyfilter(value, arg):
    return "pyfilter%s%s"%(value[::-1], arg[::-1])

register.filter("pyfilter", pyfilter)