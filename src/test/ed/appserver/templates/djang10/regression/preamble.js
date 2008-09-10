
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
TemplateSyntaxError = djang10.TemplateSyntaxError;

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

UTF8Class = function(){};
UTF8Class.prototype.toString = function() {
    return "ŠĐĆŽćžšđ";
};

from_now = function(sec_offset) {
    var now = new Date();
    now.setSeconds(now.getSeconds() + sec_offset);
    return now;
};