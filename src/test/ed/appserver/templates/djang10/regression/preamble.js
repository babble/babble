
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

function HackTemplate(content) {
    this.content = content;
};
TemplateSyntaxError = djang10.TemplateSyntaxError;

function SomeException() { }
SomeException.prototype = {
    silent_variable_failure : true
};
function SomeOtherException() {}

function NativeExceptionWrapper(className) {
    this.className = className;
};

function ExceptionStack(stack) {
    this.stack = stack;
}

function SomeClass() {
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

function OtherClass() {};
OtherClass.prototype = {
    method: function() {
        return "OtherClass.method";
    }
};

function UnsafeClass() {};
UnsafeClass.prototype.toString = function() {
    return "you & me";
};

function SafeClass() {};
SafeClass.prototype.toString = function() {
    return djang10.mark_safe("you &gt; me");
};

function UTF8Class(){};
UTF8Class.prototype.toString = function() {
    return "ŠĐĆŽćžšđ";
};

function from_now(sec_offset) {
    var now = new Date();
    now.setSeconds(now.getSeconds() + sec_offset);
    return now;
};