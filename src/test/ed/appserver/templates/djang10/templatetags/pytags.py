import _10gen

global register
register = _10gen.djang10.Library()

def pyfilter(value, arg):
    return "%s%s"%(value[::-1], arg[::-1])

register.filter("pyfilter", pyfilter)