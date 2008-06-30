import sys
import re
from django.utils import safestring


#patch Template to save the damn source
from django.template import Template 
class HackTemplate(Template):
    def __init__(self, template_string, **kwargs):
        self.template_string = template_string
        Template.__init__(self, template_string, **kwargs)

import django.template
django.template.Template = HackTemplate


from django import template
from django.template import TemplateSyntaxError
from regressiontests.templates  import tests




exported_classes = (
    tests.SomeException,
    tests.SomeOtherException,
    tests.SomeClass,
    tests.OtherClass,
    
    TemplateSyntaxError,
    HackTemplate,   # requires args
)

unsupported_tests = (
    r'^url05$',
    r'^i18n',
    r'^filter-syntax18$',
    r'autoescape-tag07',
    r'autoescape-tag09',
    r'autoescape-tag10',
)


preamble = """
HackTemplate = function(content) {
    this.content = content;
};
TemplateSyntaxError = function() {};

SomeException = function() { }
SomeException.prototype = {
    silent_variable_failure : true
};
SomeOtherException = function() {}


SomeClass = function() {
    this.otherclass = new OtherClass();
};
SomeClass.prototype = {
    method: function() {
        return "SomeClass.method";
    },
    method2: function(o) {
        return this.o;
    },
    method3: function() {
        throw new SomeException();
    },
    method4: function() {
        throw new SomeOtherException();
    }
};

OtherClass = function() {};
OtherClass.prototype = {
    method: function() {
        return "OtherClass.method";
    }
};
"""


def convert(py_tests):
    #ignoring filter_tests
    expected_invalid_str = 'INVALID'
    
    
    buffer = preamble
    buffer += "tests=[\n"
    
    for name, vals in py_tests:
        if [pattern for pattern in unsupported_tests if re.search(pattern, name)]:
            continue
        
        if isinstance(vals[2], tuple):
            normal_string_result = vals[2][0]
            invalid_string_result = vals[2][1]
            if '%s' in invalid_string_result:
                expected_invalid_str = 'INVALID %s'
                invalid_string_result = invalid_string_result % vals[2][2]
            else:
                normal_string_result = vals[2]
                invalid_string_result = vals[2]
            
            #ignoring LANGUAGE_CORE for now
        buffer += serialize_test(name, vals) + ",\n" 
    
    return buffer + "\n];"


def serialize_test(name, a_test):
    #special case dates
    if name == 'now01':
        results =  '%s + " " + %s + " " + %s' % ("((new Date()).getDate())", "((new Date()).getMonth() + 1)", "((new Date()).getYear())") 
    else:
        results = serialize(a_test[2])
    #rename var to var1
    content = a_test[0]
    content = escape_var(content)
    return '    { name: %s, content: %s, model: %s, results: %s }' % ( serialize(name), serialize(content), serialize(a_test[1]), results)

def serialize(m):
    if m is None:
        return "null"

    elif isinstance(m, (tuple, list)):
        return "[ %s ]" % ", ".join( ["%s" % serialize(item) for item in m] )

    elif isinstance(m, dict):
        return '{ %s }' % ", ".join( ['%s: %s' % (serialize(escape_var(key)), serialize(value)) for key, value in m.items()] )

    elif isinstance(m, str):
        return '"%s"' % escape_str(m)
    
    elif isinstance(m , (int, long) ):
        return "%d" % m
    
    elif isinstance(m, float):
        return "%f" % m

    elif isinstance(m, type):
        if(m in exported_classes):
            return m.__name__
        raise Exception("can't serialize the type: %s" % m)

    elif isinstance(m, object):
        if(isinstance(m, HackTemplate)):
            return 'new %s(%s)' % (m.__class__.__name__, serialize(m.template_string))

        if(m.__class__ in exported_classes):
            return "new %s()" % m.__class__.__name__
        else:
            raise Exception("Can't serialize the obj: %s"  % m.__class__)
        
    else:
        raise Exception("can't serialize the model: %s" % m)



def escape_var(var):
    if isinstance(var, str):
        return re.sub(r'([^\w]|^)var([^\w]|$)', r"\1var_ex\2", var)
    return var

def escape_str(str):
    def replace(match):
        return ESCAPE_DCT[match.group(0)]
    return ESCAPE.sub(replace, str)

''' String escaping'''
ESCAPE = re.compile(r'[\x00-\x1f\\"\b\f\n\r\t]')
ESCAPE_DCT = {
    '\\': '\\\\',
    '"': '\\"',
    '\b': '\\b',
    '\f': '\\f',
    '\n': '\\n',
    '\r': '\\r',
    '\t': '\\t',
}
for i in range(0x20):
    ESCAPE_DCT.setdefault(chr(i), '\\u%04x' % (i,))



''' Main '''
items = tests.Templates("test_templates").get_template_tests().items()
items.sort()
result = convert(items)
print result
    