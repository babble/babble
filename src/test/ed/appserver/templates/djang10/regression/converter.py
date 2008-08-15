'''
    Copyright (C) 2008 10gen Inc.
  
    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
  
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
'''

from __future__ import with_statement
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

#patch datetime.now
import datetime
olddatetime = datetime.datetime
class HackDatetime(datetime.datetime):
    @classmethod
    def now(cls, *args, **kw):
        wrapped = HackDatetime.wrap(olddatetime.now(*args, **kw))
        wrapped.delta = datetime.timedelta()
        return wrapped
    
    def __add__(self, delta):
        wrapper = HackDatetime.wrap(olddatetime.__add__(self, delta))
        wrapper.delta = self.delta + delta
        return wrapper 
    
    def __sub__(self, delta):
        wrapper = HackDatetime.wrap(olddatetime.__sub__(self, delta))
        wrapper.delta = self.delta - delta
        return wrapper
    
    @classmethod
    def wrap(cls, dt):
        return HackDatetime(dt.year, dt.month, dt.day, dt.hour, dt.minute, dt.second, dt.microsecond, dt.tzinfo)

datetime.datetime = HackDatetime

#import django stuff
from django.utils.safestring import SafeData, EscapeString
from django.template import TemplateSyntaxError
from regressiontests.templates  import tests
from regressiontests.templates import filters



exported_classes = (
    tests.SomeException,
    tests.SomeOtherException,
    tests.SomeClass,
    tests.OtherClass,
    
    filters.SafeClass,
    filters.UnsafeClass,
    
    TemplateSyntaxError,
    HackTemplate,   # requires args
)

unsupported_tests = (
    r'^url05$',
    r'^i18n',
    r'^filter-syntax18$',
    
    r'autoescape-stringfilter01',
)


preamble = """
/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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

UnsafeClass = function() {};
UnsafeClass.prototype.toString = function() {
    return "you & me";
};

SafeClass = function() {};
SafeClass.prototype.toString = function() {
    return djang10.mark_safe("you &gt; me");
};

var from_now = function(sec_offset) {
    var now = new Date();
    now.setSeconds(now.getSeconds() + sec_offset);
    return now;
};
"""


def convert(py_tests):
    #ignoring filter_tests
    expected_invalid_str = 'INVALID'
    
    
    buffer = preamble
    buffer += "tests=[\n"
    
    skip_count = 0
    for name, vals in py_tests:
        if [pattern for pattern in unsupported_tests if re.search(pattern, name)]:
            skip_count += 1
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
    
    print("Skipping %d tests, out of %d" % (skip_count, len(py_tests)))
    return buffer + "\n];"


def serialize_test(name, a_test):
    #special case dates
    if name == 'now01':
        results =  '%s + " " + %s + " " + %s' % ("((new Date()).getDate())", "((new Date()).getMonth() + 1)", "((new Date()).getYear())") 
    else:
        results = serialize(a_test[2])
    #rename var to var1
    content = a_test[0]
    return '    { name: %s, content: %s, model: %s, results: %s }' % ( serialize(name), serialize(content), serialize(a_test[1]), results)

def serialize(m):
    if m is None:
        return "null"

    elif isinstance(m, (tuple, list)):
        return "[ %s ]" % ", ".join( ["%s" % serialize(item) for item in m] )

    elif isinstance(m, dict):
        return '{ %s }' % ", ".join( ['%s: %s' % (serialize(key), serialize(value)) for key, value in m.items()] )
    
    elif isinstance(m, SafeData):
        return 'djang10.mark_safe(%s)' % serialize(str(m))

    elif isinstance(m, HackDatetime):
        secs = m.delta.days * 3600 * 24
        secs += m.delta.seconds
        
        return "from_now(%d)" % secs;

    elif isinstance(m, unicode):
        return serialize(str(m))

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
#do tag tests
print("converting tag tests")
items = tests.Templates("test_templates").get_template_tests().items()
items.sort()

with open("tests.js", "w") as f:
    result = convert(items)
    f.writelines(result)


#do filter tests
print("\nconverting filter tests")
items = filters.get_filter_tests().items()
items.sort()

with open("filter_tests.js", "w") as f:
    result = convert(items)
    f.writelines(result)
    
