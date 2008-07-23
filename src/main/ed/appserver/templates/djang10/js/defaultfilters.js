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

var defaultfilters =
    djang10.defaultfilters = 
    {};

register = new djang10.Library();



var addslashes =
    defaultfilters.addslashes =
    function(value) {

    return value.replace("\\", "\\\\").replace('"', '\\"').replace("'", "\\'");
};
addslashes.is_safe = true;

var capfirst =
    defaultfilters.capfirst =
    function(value) {

    if(!value)
        return value;

    return value[0].toUpperCase() + value.substring(1);
};
capfirst.is_safe = true;

var _js_escapes = [
    ['\\', '\\\\'],
    ['"', '\\"'],
    ["'", "\\'"],
    ['\n', '\\n'],
    ['\r', '\\r'],
    ['\b', '\\b'],
    ['\f', '\\f'],
    ['\t', '\\t'],
    ['\v', '\\v'],
    ['</', '<\\/']
];
var escapejs =
    defaultfilters.escapejs =
    function(value) {

    for(var i=0; i<_js_escapes.length; i++)
        value = value.replace(_js_escapes[i][0], _js_escapes[i][1]);
    
    return value;
};

var fix_ampersands =
    defaultfilters.fix_ampersands =
    function(value){

    return value.replace(/&(?!(\w+|#\d+);)/, "&amp;");
};
fix_ampersands.is_safe = true;

var floatformat =
    defaultfilters.floatformat =
    function(text, arg){

    if(arg == null)
        arg = -1;

    var num;
    
    try {
        num = parseFloat(text);
    } catch(e) {
        return "";
    }
    try {
        arg = parseInt(arg);
    } catch(e) {
        return num;
    }
    
    var diff = num - parseInt(num);
    if(isNaN(diff))
        return num;

    if((diff == 0) && (arg < 0))
        return djang10.mark_safe("" + parseInt(num));
    else
        return djang10.mark_safe("" + num.toFixed(Math.abs(arg))); 
};
floatformat.is_safe = true;

//TODO: iriencode

var _zero_pad = function(num, width) {
    var zero_count = Math.max(0, width - num.toString().length);
    var buff = "";

    while(zero_count-- > 0)
        buff += "0";

    return buff + num;
};
var linenumbers =
    defaultfilters.linenumbers =
    function(value, autoescape) {

    var lines = value.split("\n");
    var width = lines.length.toString().length;
    autoescape = autoescape && !djang10.is_safe(value);
    
    for(var i=0; i<lines.length; i++) {
        var line = autoescape? escapeHTML(lines[i]) : lines[i];
        
        lines[i] = _zero_pad(i + 1, width) + ". " + line;
    }
    return djang10.mark_safe(lines.join("\n"));
};
linenumbers.is_safe = true;
linenumbers.needs_autoescape = true;

var lower =
    defaultfilters.lower =
    function(value) {

    return value.toLowerCase();
};
lower.is_safe = true;

//FIXME: js & python representations of arrays is different so this diverges from django output
var make_list =
    defaultfilters.make_list =
    function(value) {

    return value.split("");
};
make_list.is_safe = false;

var upper =
    defaultfilters.upper =
    function(value) {
        
        return value.toUpperCase();
};

var truncatewords =
    defaultfilters.truncatewords =
    function(value, arg) {

    try {
        var length = parseInt(arg);
        var words = value.split(/\s+/);
        
        if(words.length > length) {
            words = words.slice(0, length);
            var lastword = words[words.length - 1];

            if(lastword.length > 2 && lastword.substring(lastword.length - 3) != "...")
                words.push("...");
        }
        return words.join(" ");
    }
    catch(e) {
        return value;
    }
};


var urlencode =
    defaultfilters.urlencode =
    function(value) {

    return scope.getParent().getParent().getParent().getParent().escape(value);      
};

var cut =
    defaultfilters.cut =
    function(value, arg) {

    return value.replace(arg, "");
};

//django calls this escape, but its name clashes
var escapeFilter =
    defaultfilters.escapeFilter =
    function(value) {
    
    return escapeHTML(value);
};

var linebreaks =
    defaultfilters.linebreaks =
    function(value, autoescape) {

    autoescape = autoescape && !djang10.is_safe(value);
    
    value = value.replace(/\r\n|\r|\n/g, "\n");
    var paras = value.split(/\n{2,}/);
    for (var i = 0; i < paras.length; i++) {
        if(autoescape)
            paras[i] = escapeHTML(paras[i].trim());
        paras[i] = "<p>" + paras[i].replace(/\n/g, "<br />") + "</p>";
    }
        
    return djang10.mark_safe("" + paras.join("\n\n"));
};
linebreaks.is_safe = true;
linebreaks.needs_autoescape = true;

var linebreaksbr =
    defaultfilters.linebreaks =
    function(value, autoescape) {

    autoescape = autoescape && !djang10.is_safe(value);
    if(autoescape)
        value = escapeHTML(value);

    return djang10.mark_safe( value.replace(/\n/g, "<br />") );
};
linebreaksbr.is_safe = true;
linebreaksbr.needs_autoescape = true;

var removetags =
    defaultfilters.removetags =
    function(value, tags) {

    var tags_re = "(" + tags.split(/\s+/).map(escape_pattern).join("|") + ")";
    var starttag_re =  new RegExp('<'+tags_re+'(/?>|(\s+[^>]*>))', "g");
    var endtag_re = new RegExp('</' + tags_re + '>', "g");
       
    value = value.replace(starttag_re, "");
    value = value.replace(endtag_re, "");
    
    return value;
};

var dictsort =
    defaultfilters.dictsort =
    function(value, arg) {

    return value.slice().sort(function(a, b) {
        var val_a = a[arg];
        var val_b = b[arg];
         
        return (val_a < val_b)? -1 : (val_a == val_b)? 0 : 1;
    });
    
};

var dictsortreversed =
    defaultfilters.dictsortreversed =
    function(value, arg) {

    return dictsort(value, arg).reverse();
};

var join =
    defaultfilters.join =
    function(value, arg) {

    if(!(value.join instanceof Function))
        return value;
    else
        return value.join(arg);        
};

var length =
    defaultfilters.length =
    function(value) {

    return value.length;
};

var length_is =
    defaultfilters.length_is =
    function(value, arg) {

    return value.length == arg;
}; 

var date =
    defaultfilters.date =
    function(value, arg) {

    return djang10.formatDate(value, arg);        
};

var default_ =
    defaultfilters.default_ =
    function(value, arg) {
        
    return (djang10.Expression.is_true(value))? value : arg;
};

var default_if_none =
    defaultfilters.default_if_none =
    function(value, arg) {
        
    return (value == null)? arg : value;
};

var yesno =
    defaultfilters.yesno =
    function(value, arg) {

    var bits = (arg || "yes,no,maybe" ).split(",");
    if(bits.length < 2)
        return value;
    
    var yes = bits[0];
    var no = bits[1];
    var maybe = bits[2] || bits[1];
    
    return (value == null)? maybe : (djang10.Expression.is_true(value))? yes : no;     
};

register.filter("lower", lower);
register.filter("upper", upper);
register.filter("urlencode", urlencode);
register.filter("escape", escapeFilter);
register.filter("dictsort", dictsort);
register.filter("dictsortreversed", dictsortreversed);
register.filter("length", length);
register.filter("length_is", length_is);
register.filter("date", date);
register.filter("default", default_);
register.filter("default_if_none", default_if_none);
register.filter("removetags", removetags);
register.filter("yesno", yesno);
register.filter("join", join);
register.filter("truncatewords", truncatewords);
register.filter("cut", cut);
register.filter("linebreaks", linebreaks);
register.filter("linebreaksbr", linebreaksbr);
register.filter("addslashes", addslashes);
register.filter("capfirst", capfirst);
register.filter("escapejs", escapejs);
register.filter("fix_ampersands",fix_ampersands);
register.filter("floatformat", floatformat);
register.filter("linenumbers", linenumbers);
register.filter("make_list", make_list);

//helpers
var escape_pattern = function(pattern) {    return pattern.replace(/([^A-Za-z0-9])/g, "\\$1");};


return defaultfilters;
