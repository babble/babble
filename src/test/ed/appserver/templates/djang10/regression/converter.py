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


exported_classes = {}
exported_classes[tests.SomeException] = "SomeException"
exported_classes[tests.SomeOtherException] = "SomeOtherException"
exported_classes[tests.SomeClass] = "SomeClass"
exported_classes[tests.OtherClass] = "OtherClass"

exported_classes[TemplateSyntaxError] = "djang10.TemplateException"
exported_classes[HackTemplate] = "Template"

def convert(py_tests):
    #ignoring filter_tests
    #ignoring the loader for now
    
    expected_invalid_str = 'INVALID'
    
    
    buffer = """
        var SomeException = function() { }
        SomeException.prototype = {
            silent_variable_failure : true
        };
        var SomeOtherException = function() {}


        var SomeClass = function() {
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
        
        var OtherClass = function() {};
        OtherClass.prototype = {
            method: function() {
                return "OtherClass.method";
            }
        };
    """
    
    buffer += "var tests=[\n"
    
    for name, vals in py_tests:
        #autoescaping not implemented yet
        if(name.startswith("autoescape-")):
            continue
        #unpacking not supported
        if(name.startswith("for-tag-unpack")):
            continue
        #dunno what to do w. unicode
        if(name in ("url05")):
            continue
        #no i18n
        if(name.startswith("i18n") or name == "filter-syntax18" ):
            continue
        
        
        if isinstance(vals[2], tuple):
            normal_string_result = vals[2][0]
            invalid_string_result = vals[2][1]
            if '%s' in invalid_string_result:
                expected_invalid_str = 'INVALID %s'
                invalid_string_result = invalid_string_result % vals[2][2]
                #template.invalid_var_format_string = True
            else:
                normal_string_result = vals[2]
                invalid_string_result = vals[2]
            
            #ignoring LANGUAGE_CORE for now
            
            #serialize the thing
        
        buffer += serialize_test(name, vals) + ",\n" 
    buffer += "\n]"
    
    return buffer


def serialize_test(name, a_test):
    return '{ name: %s, content: %s, model: %s, results: %s }' % ( serialize(name), serialize(a_test[0]), serialize(a_test[1]), serialize(a_test[2]))

def serialize(m):
    if m is None:
        return "null"

    elif isinstance(m, (tuple, list)):
        return "[ %s ]" % ", ".join( ["%s" % serialize(item) for item in m] )

    elif isinstance(m, dict):
        return '{ %s }' % ", ".join( ['%s: %s' % (serialize(key), serialize(value)) for key, value in m.items()] )

    elif isinstance(m, str):
        def replace(match):
            return ESCAPE_DCT[match.group(0)]
        return '"%s"' % ESCAPE.sub(replace, m)
    
    elif isinstance(m, (int, long, float, complex)):
        return "%d" % m

    elif isinstance(m, type):
        if(m in exported_classes):
            return exported_classes[m]
        raise Exception("can't serialize the type: %s" % m)

    elif isinstance(m, object):
        if(m.__class__ == HackTemplate):
            return 'new %s(%s))' % (exported_classes[HackTemplate], serialize(m.template_string))

        if(m.__class__ in exported_classes):
            return "new %s()" % exported_classes[m.__class__]
        else:
            raise Exception("Can't serialize the obj: %s"  % m.__class__)
        
    else:
        raise Exception("can't serialize the model: %s" % m)



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
    