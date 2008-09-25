import os
import sys
import _10gen
import foo

def escape_html(s): return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

def cutoff(s, n=100):
    if len(s) > n: return s[:n]+ '.. cut ..'
    return s

def handler(environ, start_response):
    writer = start_response("200 OK", [ ('content-type', 'text/html') ])
    response_parts = []
    response_parts.append("<p>Modjy servlet running correctly: jython %s on %s:</p>" % (sys.version, sys.platform))
    response_parts.append("<h3>Hello WSGI World!</h3>")
    response_parts.append("<h4>Here are the contents of the WSGI environment</h4>")
    environ_str = "<table border='1'>"
    environ['10gen.db'] = _10gen.db
    environ['10gen.foo.hi'] = foo.hi
    keys = environ.keys()
    keys.sort()
    ix = 0
    for name in keys:
        if ix % 2:
            background='#ffffff'
        else:
            background='#eeeeee'
        style = " style='background-color:%s;'" % background
        value = escape_html(cutoff(str(environ[name]))) or '&#160;'
        environ_str = "%s<tr><td%s>%s</td><td%s>%s</td></tr>" % \
            (environ_str, style, name, style, value)
        ix += 1
    environ_str = "%s</table>" % environ_str
    response_parts.append(environ_str)
    response_text = "".join(response_parts)
    return [response_text]

def start_with_one_arg(environ, start_response):
    writer = start_response("200 OK")
    return []

def start_with_three_args(environ, start_response):
    writer = start_response("200 OK", [], None)
    return []

def start_with_int_first_arg(environ, start_response):
    writer = start_response(200, [])
    return []

def start_with_float_first_arg(environ, start_response):
    writer = start_response(200.0, [])
    return []

def start_with_string_second_arg(environ, start_response):
    writer = start_response("200 OK", 'content-type: text/plain')
    return []

def start_with_tuple_second_arg(environ, start_response):
    writer = start_response("200 OK", () )
    return []

def start_with_single_header_string(environ, start_response):
    writer = start_response("200 OK", ['content-type: text/plain'] )
    return []

def start_with_non_tuple_pair(environ, start_response):
    writer = start_response("200 OK", ['content-type', 'text/plain'] )
    return []

def writer_with_no_args(environ, start_response):
    writer = start_response("200 OK", [('content-type', 'text/plain')] )
    writer()
    return []

def writer_with_int_arg(environ, start_response):
    writer = start_response("200 OK", [('content-type', 'text/plain')] )
    writer(42)
    return []

def return_none(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return None

def return_no_start(environ, start_response):
    pass
    return ['hello', 'world']

def return_list(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return ['return', ':', 'list']

def return_tuple(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return ('return',  ':', 'tuple')

def return_listcomp(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return [s for s in ['return',  ':', 'listcomp']]

def return_string(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return "A single string should be acceptable"

def return_int(environ, start_response):
    start_response("200 OK", [('content-type', 'text/plain')] )
    return 42

class return_iter_wrong_length:

    def __init__(self, environ, start_response):
        start_response("200 OK", [('content-type', 'text/plain')] )

    def __len__(self):
        return 2

    def __getitem__(self, ix):
        if ix == 0:
            return "a string"
        else:
            raise IndexError()

class return_not_iter:

    def __init__(self, environ, start_response):
        start_response("200 OK", [('content-type', 'text/plain')] )

