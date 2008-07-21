
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

SafeData = function(str) {
    this.str = str;
};

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
    return new SafeData("you &gt; me");
};

var from_now = function(sec_offset) {
    var now = new Date();
    now.setSeconds(now.getSeconds() + sec_offset);
    return now;
};
tests=[
    { name: "autoescape-stringfilter02", content: "{% autoescape off %}{{ unsafe|capfirst }}{% endautoescape %}", model: { "unsafe": new UnsafeClass() }, results: "You & me" },
    { name: "autoescape-stringfilter03", content: "{{ safe|capfirst }}", model: { "safe": new SafeClass() }, results: "You &gt; me" },
    { name: "autoescape-stringfilter04", content: "{% autoescape off %}{{ safe|capfirst }}{% endautoescape %}", model: { "safe": new SafeClass() }, results: "You &gt; me" },
    { name: "chaining01", content: "{{ a|capfirst|center:\"7\" }}.{{ b|capfirst|center:\"7\" }}", model: { "a": "a < b", "b": new SafeData("a < b") }, results: " A &lt; b . A < b " },
    { name: "chaining02", content: "{% autoescape off %}{{ a|capfirst|center:\"7\" }}.{{ b|capfirst|center:\"7\" }}{% endautoescape %}", model: { "a": "a < b", "b": new SafeData("a < b") }, results: " A < b . A < b " },
    { name: "chaining03", content: "{{ a|cut:\"b\"|capfirst }}.{{ b|cut:\"b\"|capfirst }}", model: { "a": "a < b", "b": new SafeData("a < b") }, results: "A &lt; .A < " },
    { name: "chaining04", content: "{% autoescape off %}{{ a|cut:\"b\"|capfirst }}.{{ b|cut:\"b\"|capfirst }}{% endautoescape %}", model: { "a": "a < b", "b": new SafeData("a < b") }, results: "A < .A < " },
    { name: "chaining05", content: "{{ a|escape|capfirst }}", model: { "a": "a < b" }, results: "A &lt; b" },
    { name: "chaining06", content: "{% autoescape off %}{{ a|escape|capfirst }}{% endautoescape %}", model: { "a": "a < b" }, results: "A &lt; b" },
    { name: "chaining07", content: "{{ a|force_escape|cut:\";\" }}", model: { "a": "a < b" }, results: "a &amp;lt b" },
    { name: "chaining08", content: "{% autoescape off %}{{ a|force_escape|cut:\";\" }}{% endautoescape %}", model: { "a": "a < b" }, results: "a &lt b" },
    { name: "chaining09", content: "{{ a|cut:\";\"|force_escape }}", model: { "a": "a < b" }, results: "a &lt; b" },
    { name: "chaining10", content: "{% autoescape off %}{{ a|cut:\";\"|force_escape }}{% endautoescape %}", model: { "a": "a < b" }, results: "a &lt; b" },
    { name: "chaining11", content: "{{ a|cut:\"b\"|safe }}", model: { "a": "a < b" }, results: "a < " },
    { name: "chaining12", content: "{% autoescape off %}{{ a|cut:\"b\"|safe }}{% endautoescape %}", model: { "a": "a < b" }, results: "a < " },
    { name: "chaining13", content: "{{ a|safe|force_escape }}", model: { "a": "a < b" }, results: "a &lt; b" },
    { name: "chaining14", content: "{% autoescape off %}{{ a|safe|force_escape }}{% endautoescape %}", model: { "a": "a < b" }, results: "a &lt; b" },
    { name: "filter-addslash01", content: "{% autoescape off %}{{ a|addslashes }} {{ b|addslashes }}{% endautoescape %}", model: { "a": "<a>'", "b": new SafeData("<a>'") }, results: "<a>\\' <a>\\'" },
    { name: "filter-addslash02", content: "{{ a|addslashes }} {{ b|addslashes }}", model: { "a": "<a>'", "b": new SafeData("<a>'") }, results: "&lt;a&gt;\\&#39; <a>\\'" },
    { name: "filter-capfirst01", content: "{% autoescape off %}{{ a|capfirst }} {{ b|capfirst }}{% endautoescape %}", model: { "a": "fred>", "b": new SafeData("fred&gt;") }, results: "Fred> Fred&gt;" },
    { name: "filter-capfirst02", content: "{{ a|capfirst }} {{ b|capfirst }}", model: { "a": "fred>", "b": new SafeData("fred&gt;") }, results: "Fred&gt; Fred&gt;" },
    { name: "filter-center01", content: "{% autoescape off %}.{{ a|center:\"5\" }}. .{{ b|center:\"5\" }}.{% endautoescape %}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ". a&b . . a&b ." },
    { name: "filter-center02", content: ".{{ a|center:\"5\" }}. .{{ b|center:\"5\" }}.", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ". a&amp;b . . a&b ." },
    { name: "filter-cut01", content: "{% autoescape off %}{{ a|cut:\"x\" }} {{ b|cut:\"x\" }}{% endautoescape %}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "&y &amp;y" },
    { name: "filter-cut02", content: "{{ a|cut:\"x\" }} {{ b|cut:\"x\" }}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "&amp;y &amp;y" },
    { name: "filter-cut03", content: "{% autoescape off %}{{ a|cut:\"&\" }} {{ b|cut:\"&\" }}{% endautoescape %}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "xy xamp;y" },
    { name: "filter-cut04", content: "{{ a|cut:\"&\" }} {{ b|cut:\"&\" }}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "xy xamp;y" },
    { name: "filter-cut05", content: "{% autoescape off %}{{ a|cut:\";\" }} {{ b|cut:\";\" }}{% endautoescape %}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "x&y x&ampy" },
    { name: "filter-cut06", content: "{{ a|cut:\";\" }} {{ b|cut:\";\" }}", model: { "a": "x&y", "b": new SafeData("x&amp;y") }, results: "x&amp;y x&amp;ampy" },
    { name: "filter-default01", content: "{{ a|default:\"x<\" }}", model: { "a": "" }, results: "x<" },
    { name: "filter-default02", content: "{% autoescape off %}{{ a|default:\"x<\" }}{% endautoescape %}", model: { "a": "" }, results: "x<" },
    { name: "filter-default03", content: "{{ a|default:\"x<\" }}", model: { "a": new SafeData("x>") }, results: "x>" },
    { name: "filter-default04", content: "{% autoescape off %}{{ a|default:\"x<\" }}{% endautoescape %}", model: { "a": new SafeData("x>") }, results: "x>" },
    { name: "filter-default_if_none01", content: "{{ a|default:\"x<\" }}", model: { "a": null }, results: "x<" },
    { name: "filter-default_if_none02", content: "{% autoescape off %}{{ a|default:\"x<\" }}{% endautoescape %}", model: { "a": null }, results: "x<" },
    { name: "filter-escape01", content: "{{ a|escape }} {{ b|escape }}", model: { "a": "x&y", "b": new SafeData("x&y") }, results: "x&amp;y x&y" },
    { name: "filter-escape02", content: "{% autoescape off %}{{ a|escape }} {{ b|escape }}{% endautoescape %}", model: { "a": "x&y", "b": new SafeData("x&y") }, results: "x&amp;y x&y" },
    { name: "filter-escape03", content: "{% autoescape off %}{{ a|escape|escape }}{% endautoescape %}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-escape04", content: "{{ a|escape|escape }}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-first01", content: "{{ a|first }} {{ b|first }}", model: { "a": [ "a&b", "x" ], "b": [ new SafeData("a&b"), "x" ] }, results: "a&amp;b a&b" },
    { name: "filter-first02", content: "{% autoescape off %}{{ a|first }} {{ b|first }}{% endautoescape %}", model: { "a": [ "a&b", "x" ], "b": [ new SafeData("a&b"), "x" ] }, results: "a&b a&b" },
    { name: "filter-fix_ampersands01", content: "{% autoescape off %}{{ a|fix_ampersands }} {{ b|fix_ampersands }}{% endautoescape %}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: "a&amp;b a&amp;b" },
    { name: "filter-fix_ampersands02", content: "{{ a|fix_ampersands }} {{ b|fix_ampersands }}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: "a&amp;amp;b a&amp;b" },
    { name: "filter-floatformat01", content: "{% autoescape off %}{{ a|floatformat }} {{ b|floatformat }}{% endautoescape %}", model: { "a": "1.42", "b": new SafeData("1.42") }, results: "1.4 1.4" },
    { name: "filter-floatformat02", content: "{{ a|floatformat }} {{ b|floatformat }}", model: { "a": "1.42", "b": new SafeData("1.42") }, results: "1.4 1.4" },
    { name: "filter-force-escape01", content: "{% autoescape off %}{{ a|force_escape }}{% endautoescape %}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-force-escape02", content: "{{ a|force_escape }}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-force-escape03", content: "{% autoescape off %}{{ a|force_escape|force_escape }}{% endautoescape %}", model: { "a": "x&y" }, results: "x&amp;amp;y" },
    { name: "filter-force-escape04", content: "{{ a|force_escape|force_escape }}", model: { "a": "x&y" }, results: "x&amp;amp;y" },
    { name: "filter-force-escape05", content: "{% autoescape off %}{{ a|force_escape|escape }}{% endautoescape %}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-force-escape06", content: "{{ a|force_escape|escape }}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-force-escape07", content: "{{ a|escape|force_escape }}", model: { "a": "x&y" }, results: "x&amp;y" },
    { name: "filter-iriencode01", content: "{{ url|iriencode }}", model: { "url": "?test=1&me=2" }, results: "?test=1&amp;me=2" },
    { name: "filter-iriencode02", content: "{% autoescape off %}{{ url|iriencode }}{% endautoescape %}", model: { "url": "?test=1&me=2" }, results: "?test=1&me=2" },
    { name: "filter-iriencode03", content: "{{ url|iriencode }}", model: { "url": new SafeData("?test=1&me=2") }, results: "?test=1&me=2" },
    { name: "filter-iriencode04", content: "{% autoescape off %}{{ url|iriencode }}{% endautoescape %}", model: { "url": new SafeData("?test=1&me=2") }, results: "?test=1&me=2" },
    { name: "filter-last01", content: "{{ a|last }} {{ b|last }}", model: { "a": [ "x", "a&b" ], "b": [ "x", new SafeData("a&b") ] }, results: "a&amp;b a&b" },
    { name: "filter-last02", content: "{% autoescape off %}{{ a|last }} {{ b|last }}{% endautoescape %}", model: { "a": [ "x", "a&b" ], "b": [ "x", new SafeData("a&b") ] }, results: "a&b a&b" },
    { name: "filter-linebreaks01", content: "{{ a|linebreaks }} {{ b|linebreaks }}", model: { "a": "x&\ny", "b": new SafeData("x&\ny") }, results: "<p>x&amp;<br />y</p> <p>x&<br />y</p>" },
    { name: "filter-linebreaks02", content: "{% autoescape off %}{{ a|linebreaks }} {{ b|linebreaks }}{% endautoescape %}", model: { "a": "x&\ny", "b": new SafeData("x&\ny") }, results: "<p>x&<br />y</p> <p>x&<br />y</p>" },
    { name: "filter-linebreaksbr01", content: "{{ a|linebreaksbr }} {{ b|linebreaksbr }}", model: { "a": "x&\ny", "b": new SafeData("x&\ny") }, results: "x&amp;<br />y x&<br />y" },
    { name: "filter-linebreaksbr02", content: "{% autoescape off %}{{ a|linebreaksbr }} {{ b|linebreaksbr }}{% endautoescape %}", model: { "a": "x&\ny", "b": new SafeData("x&\ny") }, results: "x&<br />y x&<br />y" },
    { name: "filter-linenumbers01", content: "{{ a|linenumbers }} {{ b|linenumbers }}", model: { "a": "one\n<two>\nthree", "b": new SafeData("one\n&lt;two&gt;\nthree") }, results: "1. one\n2. &lt;two&gt;\n3. three 1. one\n2. &lt;two&gt;\n3. three" },
    { name: "filter-linenumbers02", content: "{% autoescape off %}{{ a|linenumbers }} {{ b|linenumbers }}{% endautoescape %}", model: { "a": "one\n<two>\nthree", "b": new SafeData("one\n&lt;two&gt;\nthree") }, results: "1. one\n2. <two>\n3. three 1. one\n2. &lt;two&gt;\n3. three" },
    { name: "filter-ljust01", content: "{% autoescape off %}.{{ a|ljust:\"5\" }}. .{{ b|ljust:\"5\" }}.{% endautoescape %}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ".a&b  . .a&b  ." },
    { name: "filter-ljust02", content: ".{{ a|ljust:\"5\" }}. .{{ b|ljust:\"5\" }}.", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ".a&amp;b  . .a&b  ." },
    { name: "filter-lower01", content: "{% autoescape off %}{{ a|lower }} {{ b|lower }}{% endautoescape %}", model: { "a": "Apple & banana", "b": new SafeData("Apple &amp; banana") }, results: "apple & banana apple &amp; banana" },
    { name: "filter-lower02", content: "{{ a|lower }} {{ b|lower }}", model: { "a": "Apple & banana", "b": new SafeData("Apple &amp; banana") }, results: "apple &amp; banana apple &amp; banana" },
    { name: "filter-make_list01", content: "{% autoescape off %}{{ a|make_list }}{% endautoescape %}", model: { "a": new SafeData("&") }, results: "[u'&']" },
    { name: "filter-make_list02", content: "{{ a|make_list }}", model: { "a": new SafeData("&") }, results: "[u&#39;&amp;&#39;]" },
    { name: "filter-make_list03", content: "{% autoescape off %}{{ a|make_list|stringformat:\"s\"|safe }}{% endautoescape %}", model: { "a": new SafeData("&") }, results: "[u'&']" },
    { name: "filter-make_list04", content: "{{ a|make_list|stringformat:\"s\"|safe }}", model: { "a": new SafeData("&") }, results: "[u'&']" },
    { name: "filter-phone2numeric01", content: "{{ a|phone2numeric }} {{ b|phone2numeric }}", model: { "a": "<1-800-call-me>", "b": new SafeData("<1-800-call-me>") }, results: "&lt;1-800-2255-63&gt; <1-800-2255-63>" },
    { name: "filter-phone2numeric02", content: "{% autoescape off %}{{ a|phone2numeric }} {{ b|phone2numeric }}{% endautoescape %}", model: { "a": "<1-800-call-me>", "b": new SafeData("<1-800-call-me>") }, results: "<1-800-2255-63> <1-800-2255-63>" },
    { name: "filter-random01", content: "{{ a|random }} {{ b|random }}", model: { "a": [ "a&b", "a&b" ], "b": [ new SafeData("a&b"), new SafeData("a&b") ] }, results: "a&amp;b a&b" },
    { name: "filter-random02", content: "{% autoescape off %}{{ a|random }} {{ b|random }}{% endautoescape %}", model: { "a": [ "a&b", "a&b" ], "b": [ new SafeData("a&b"), new SafeData("a&b") ] }, results: "a&b a&b" },
    { name: "filter-removetags01", content: "{{ a|removetags:\"a b\" }} {{ b|removetags:\"a b\" }}", model: { "a": "<a>x</a> <p><b>y</b></p>", "b": new SafeData("<a>x</a> <p><b>y</b></p>") }, results: "x &lt;p&gt;y&lt;/p&gt; x <p>y</p>" },
    { name: "filter-removetags02", content: "{% autoescape off %}{{ a|removetags:\"a b\" }} {{ b|removetags:\"a b\" }}{% endautoescape %}", model: { "a": "<a>x</a> <p><b>y</b></p>", "b": new SafeData("<a>x</a> <p><b>y</b></p>") }, results: "x <p>y</p> x <p>y</p>" },
    { name: "filter-rjust01", content: "{% autoescape off %}.{{ a|rjust:\"5\" }}. .{{ b|rjust:\"5\" }}.{% endautoescape %}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ".  a&b. .  a&b." },
    { name: "filter-rjust02", content: ".{{ a|rjust:\"5\" }}. .{{ b|rjust:\"5\" }}.", model: { "a": "a&b", "b": new SafeData("a&b") }, results: ".  a&amp;b. .  a&b." },
    { name: "filter-safe01", content: "{{ a }} -- {{ a|safe }}", model: { "a": "<b>hello</b>" }, results: "&lt;b&gt;hello&lt;/b&gt; -- <b>hello</b>" },
    { name: "filter-safe02", content: "{% autoescape off %}{{ a }} -- {{ a|safe }}{% endautoescape %}", model: { "a": "<b>hello</b>" }, results: "<b>hello</b> -- <b>hello</b>" },
    { name: "filter-slice01", content: "{{ a|slice:\"1:3\" }} {{ b|slice:\"1:3\" }}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: "&amp;b &b" },
    { name: "filter-slice02", content: "{% autoescape off %}{{ a|slice:\"1:3\" }} {{ b|slice:\"1:3\" }}{% endautoescape %}", model: { "a": "a&b", "b": new SafeData("a&b") }, results: "&b &b" },
    { name: "filter-slugify01", content: "{% autoescape off %}{{ a|slugify }} {{ b|slugify }}{% endautoescape %}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "a-b a-amp-b" },
    { name: "filter-slugify02", content: "{{ a|slugify }} {{ b|slugify }}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "a-b a-amp-b" },
    { name: "filter-stringformat01", content: "{% autoescape off %}.{{ a|stringformat:\"5s\" }}. .{{ b|stringformat:\"5s\" }}.{% endautoescape %}", model: { "a": "a<b", "b": new SafeData("a<b") }, results: ".  a<b. .  a<b." },
    { name: "filter-stringformat02", content: ".{{ a|stringformat:\"5s\" }}. .{{ b|stringformat:\"5s\" }}.", model: { "a": "a<b", "b": new SafeData("a<b") }, results: ".  a&lt;b. .  a<b." },
    { name: "filter-striptags01", content: "{{ a|striptags }} {{ b|striptags }}", model: { "a": "<a>x</a> <p><b>y</b></p>", "b": new SafeData("<a>x</a> <p><b>y</b></p>") }, results: "x y x y" },
    { name: "filter-striptags02", content: "{% autoescape off %}{{ a|striptags }} {{ b|striptags }}{% endautoescape %}", model: { "a": "<a>x</a> <p><b>y</b></p>", "b": new SafeData("<a>x</a> <p><b>y</b></p>") }, results: "x y x y" },
    { name: "filter-timesince01", content: "{{ a|timesince }}", model: { "a": from_now(-70) }, results: "1 minute" },
    { name: "filter-timesince02", content: "{{ a|timesince }}", model: { "a": from_now(-86460) }, results: "1 day" },
    { name: "filter-timesince03", content: "{{ a|timesince }}", model: { "a": from_now(-5110) }, results: "1 hour, 25 minutes" },
    { name: "filter-timesince04", content: "{{ a|timesince:b }}", model: { "a": from_now(172800), "b": from_now(86400) }, results: "1 day" },
    { name: "filter-timesince05", content: "{{ a|timesince:b }}", model: { "a": from_now(172860), "b": from_now(172800) }, results: "1 minute" },
    { name: "filter-timesince06", content: "{{ a|timesince:b }}", model: { "a": from_now(28800), "b": from_now(0) }, results: "8 hours" },
    { name: "filter-timeuntil01", content: "{{ a|timeuntil }}", model: { "a": from_now(130) }, results: "2 minutes" },
    { name: "filter-timeuntil02", content: "{{ a|timeuntil }}", model: { "a": from_now(86410) }, results: "1 day" },
    { name: "filter-timeuntil03", content: "{{ a|timeuntil }}", model: { "a": from_now(29410) }, results: "8 hours, 10 minutes" },
    { name: "filter-timeuntil04", content: "{{ a|timeuntil:b }}", model: { "a": from_now(-86400), "b": from_now(-172800) }, results: "1 day" },
    { name: "filter-timeuntil05", content: "{{ a|timeuntil:b }}", model: { "a": from_now(-172800), "b": from_now(-172860) }, results: "1 minute" },
    { name: "filter-truncatewords01", content: "{% autoescape off %}{{ a|truncatewords:\"2\" }} {{ b|truncatewords:\"2\"}}{% endautoescape %}", model: { "a": "alpha & bravo", "b": new SafeData("alpha &amp; bravo") }, results: "alpha & ... alpha &amp; ..." },
    { name: "filter-truncatewords02", content: "{{ a|truncatewords:\"2\" }} {{ b|truncatewords:\"2\"}}", model: { "a": "alpha & bravo", "b": new SafeData("alpha &amp; bravo") }, results: "alpha &amp; ... alpha &amp; ..." },
    { name: "filter-unordered_list01", content: "{{ a|unordered_list }}", model: { "a": [ "x>", [ [ "<y", [  ] ] ] ] }, results: "\t<li>x&gt;\n\t<ul>\n\t\t<li>&lt;y</li>\n\t</ul>\n\t</li>" },
    { name: "filter-unordered_list02", content: "{% autoescape off %}{{ a|unordered_list }}{% endautoescape %}", model: { "a": [ "x>", [ [ "<y", [  ] ] ] ] }, results: "\t<li>x>\n\t<ul>\n\t\t<li><y</li>\n\t</ul>\n\t</li>" },
    { name: "filter-unordered_list03", content: "{{ a|unordered_list }}", model: { "a": [ "x>", [ [ new SafeData("<y"), [  ] ] ] ] }, results: "\t<li>x&gt;\n\t<ul>\n\t\t<li><y</li>\n\t</ul>\n\t</li>" },
    { name: "filter-unordered_list04", content: "{% autoescape off %}{{ a|unordered_list }}{% endautoescape %}", model: { "a": [ "x>", [ [ new SafeData("<y"), [  ] ] ] ] }, results: "\t<li>x>\n\t<ul>\n\t\t<li><y</li>\n\t</ul>\n\t</li>" },
    { name: "filter-unordered_list05", content: "{% autoescape off %}{{ a|unordered_list }}{% endautoescape %}", model: { "a": [ "x>", [ [ "<y", [  ] ] ] ] }, results: "\t<li>x>\n\t<ul>\n\t\t<li><y</li>\n\t</ul>\n\t</li>" },
    { name: "filter-upper01", content: "{% autoescape off %}{{ a|upper }} {{ b|upper }}{% endautoescape %}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "A & B A &AMP; B" },
    { name: "filter-upper02", content: "{{ a|upper }} {{ b|upper }}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "A &amp; B A &amp;AMP; B" },
    { name: "filter-urlize01", content: "{% autoescape off %}{{ a|urlize }} {{ b|urlize }}{% endautoescape %}", model: { "a": "http://example.com/?x=&y=", "b": new SafeData("http://example.com?x=&amp;y=") }, results: "<a href=\"http://example.com/?x=&y=\" rel=\"nofollow\">http://example.com/?x=&y=</a> <a href=\"http://example.com?x=&amp;y=\" rel=\"nofollow\">http://example.com?x=&amp;y=</a>" },
    { name: "filter-urlize02", content: "{{ a|urlize }} {{ b|urlize }}", model: { "a": "http://example.com/?x=&y=", "b": new SafeData("http://example.com?x=&amp;y=") }, results: "<a href=\"http://example.com/?x=&amp;y=\" rel=\"nofollow\">http://example.com/?x=&amp;y=</a> <a href=\"http://example.com?x=&amp;y=\" rel=\"nofollow\">http://example.com?x=&amp;y=</a>" },
    { name: "filter-urlize03", content: "{% autoescape off %}{{ a|urlize }}{% endautoescape %}", model: { "a": new SafeData("a &amp; b") }, results: "a &amp; b" },
    { name: "filter-urlize04", content: "{{ a|urlize }}", model: { "a": new SafeData("a &amp; b") }, results: "a &amp; b" },
    { name: "filter-urlize05", content: "{% autoescape off %}{{ a|urlize }}{% endautoescape %}", model: { "a": "<script>alert('foo')</script>" }, results: "<script>alert('foo')</script>" },
    { name: "filter-urlize06", content: "{{ a|urlize }}", model: { "a": "<script>alert('foo')</script>" }, results: "&lt;script&gt;alert(&#39;foo&#39;)&lt;/script&gt;" },
    { name: "filter-urlize07", content: "{{ a|urlize }}", model: { "a": "Email me at me@example.com" }, results: "Email me at <a href=\"mailto:me@example.com\">me@example.com</a>" },
    { name: "filter-urlize08", content: "{{ a|urlize }}", model: { "a": "Email me at <me@example.com>" }, results: "Email me at &lt;<a href=\"mailto:me@example.com\">me@example.com</a>&gt;" },
    { name: "filter-urlizetrunc01", content: "{% autoescape off %}{{ a|urlizetrunc:\"8\" }} {{ b|urlizetrunc:\"8\" }}{% endautoescape %}", model: { "a": "\"Unsafe\" http://example.com/x=&y=", "b": new SafeData("&quot;Safe&quot; http://example.com?x=&amp;y=") }, results: "\"Unsafe\" <a href=\"http://example.com/x=&y=\" rel=\"nofollow\">http:...</a> &quot;Safe&quot; <a href=\"http://example.com?x=&amp;y=\" rel=\"nofollow\">http:...</a>" },
    { name: "filter-urlizetrunc02", content: "{{ a|urlizetrunc:\"8\" }} {{ b|urlizetrunc:\"8\" }}", model: { "a": "\"Unsafe\" http://example.com/x=&y=", "b": new SafeData("&quot;Safe&quot; http://example.com?x=&amp;y=") }, results: "&quot;Unsafe&quot; <a href=\"http://example.com/x=&amp;y=\" rel=\"nofollow\">http:...</a> &quot;Safe&quot; <a href=\"http://example.com?x=&amp;y=\" rel=\"nofollow\">http:...</a>" },
    { name: "filter-wordcount01", content: "{% autoescape off %}{{ a|wordcount }} {{ b|wordcount }}{% endautoescape %}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "3 3" },
    { name: "filter-wordcount02", content: "{{ a|wordcount }} {{ b|wordcount }}", model: { "a": "a & b", "b": new SafeData("a &amp; b") }, results: "3 3" },
    { name: "filter-wordwrap01", content: "{% autoescape off %}{{ a|wordwrap:\"3\" }} {{ b|wordwrap:\"3\" }}{% endautoescape %}", model: { "a": "a & b", "b": new SafeData("a & b") }, results: "a &\nb a &\nb" },
    { name: "filter-wordwrap02", content: "{{ a|wordwrap:\"3\" }} {{ b|wordwrap:\"3\" }}", model: { "a": "a & b", "b": new SafeData("a & b") }, results: "a &amp;\nb a &\nb" },

];
