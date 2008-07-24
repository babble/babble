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


///////////////////////
// STRING DECORATOR  //
///////////////////////

var stringfilter =
    defaultfilters.stringfilter =
    function(func) {

    var f = function() {
        if (arguments[0] != null && !(arguments[0] instanceof String))
            arguments[0] = arguments[0].toString();
        
        var result = func.apply(null, arguments); 
        
        if(djang10.is_safe(arguments[0]) && func.is_safe)
            result = djang10.mark_safe(result);

        return result; 
    };
    
    ["is_safe", "needs_autoescape"].each(function(k) {
        if(k in func)
            f[k] = func[k];
    });
    
    return f;
};

///////////////////////
// STRINGS           //
///////////////////////

var addslashes =
    defaultfilters.addslashes =
    function(value) {

    return value.replace("\\", "\\\\").replace('"', '\\"').replace("'", "\\'");
};
addslashes.is_safe = true;
addslashes = defaultfilters.addslashes = stringfilter(addslashes);

var capfirst =
    defaultfilters.capfirst =
    function(value) {

    if(!value)
        return value;

    return value[0].toUpperCase() + value.substring(1);
};
capfirst.is_safe = true;
capfirst = defaultfilters.capfirst = stringfilter(capfirst);

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
escapejs = defaultfilters.escapejs = stringfilter(escapejs);

var fix_ampersands =
    defaultfilters.fix_ampersands =
    function(value){

    return value.replace(/&(?!(\w+|#\d+);)/, "&amp;");
};
fix_ampersands.is_safe = true;
fix_ampersands = defaultfilters.fix_ampersands = stringfilter(fix_ampersands);

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
linenumbers = defaultfilters.linenumbers = stringfilter(linenumbers);

var lower =
    defaultfilters.lower =
    function(value) {

    return value.toLowerCase();
};
lower.is_safe = true;
lower = defaultfilters.lower = stringfilter(lower);

//FIXME: js & python representations of arrays is different so this diverges from django output
var make_list =
    defaultfilters.make_list =
    function(value) {

    return value.split("");
};
make_list.is_safe = false;
make_list = defaultfilters.make_list = stringfilter(make_list);

//TODO: slugify
//TODO: stringformat
//TODO: title

var truncatewords =
    defaultfilters.truncatewords =
    function(value, arg) {

    var length = parseInt(arg);
    if(isNaN(length))
        return value;
    
    var words = value.split(/\s+/);
    if(words.length > length) {
        words = words.slice(0, length);
        var lastword = words[words.length - 1];

        if(lastword.substring(lastword.length - 3) != "...")
            words.push("...");
    }
    return words.join(" ");    
};
truncatewords.is_safe = true;
truncatewords = defaultfilters.truncatewords = stringfilter(truncatewords);

//TODO: truncatewords_html

var upper =
    defaultfilters.upper =
    function(value) {
        
        return value.toUpperCase();
};
upper.is_safe = false;
upper = defaultfilters.upper = stringfilter(upper);

var urlencode =
    defaultfilters.urlencode =
    function(value) {

    return scope.getParent().getParent().getParent().getParent().escape(value);      
};
urlencode.is_safe = true;
urlencode = defaultfilters.urlencode = stringfilter(urlencode);

//TODO: urlize
//TODO: urlizetrunc

var wordcount =
    defaultfilters.wordcount =
    function(value) {

    return value.split(/\s+/).length;
};
wordcount.is_safe = false;
wordcount = defaultfilters.wordcount = stringfilter(wordcount);

//TODO: wordwrap

var ljust =
    defaultfilters.ljust =
    function(value, arg) {

    var width = parseInt(arg);
    var buffer = "";
    
    var nspaces = Math.max(0, width - value.length);
    while(nspaces-- > 0)
        buffer += " ";

    return value + buffer; 
};
ljust.is_safe = true;
ljust = defaultfilters.ljust = stringfilter(ljust);

var rjust =
    defaultfilters.rjust =
    function(value, arg) {

    var width = parseInt(arg);
    var buffer = "";
    
    var nspaces = Math.max(0, width - value.length);
    while(nspaces-- > 0)
        buffer += " ";

    return buffer + value; 
};
rjust.is_safe = true;
rjust = defaultfilters.rjust = stringfilter(rjust);

var center =
    defaultfilters.center =
    function(value, arg) {

    var width = parseInt(arg);
    
    var nspaces = Math.max(0, width - value.length);
    
    var leftspaces = nspaces/2;
    var rightspaces = nspaces - leftspaces;
    
    value = ljust(value, rightspaces + value.length);
    value = rjust(value, leftspaces + value.length);
    
    return value;
};
center.is_safe = true;
center = defaultfilters.center = stringfilter(center);

var cut =
    defaultfilters.cut =
    function(value, arg) {

    var safe = djang10.is_safe(value);
    value = value.replace(arg, "");

    if(safe && (arg != ";"))
        return djang10.mark_safe(value);

    return value;
};
cut = defaultfilters.cut = stringfilter(cut);



///////////////////////
// HTML STRINGS      //
///////////////////////

var escape_ =
    defaultfilters.escape =
    function(value) {
    
    return djang10.mark_escape(value);
};
escape_.is_safe = true;
escape_ = defaultfilters.escape = stringfilter(escape_);

var force_escape =
    defaultfilters.force_escape =
    function(value) {

    return djang10.mark_safe(escapeHTML(value));        
};
force_escape.is_safe = true;
force_escape = defaultfilters.force_escape = stringfilter(force_escape);

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
linebreaks = defaultfilters.linebreaks = stringfilter(linebreaks);

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
linebreaksbr = defaultfilters.linebreaksbr = stringfilter(linebreaksbr);

var safe =
    defaultfilters.safe =
    function(value) {

    return djang10.mark_safe(value);
};
safe.is_safe = true;
safe = defaultfilters.safe = stringfilter(safe);

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
removetags.is_safe = true;
removetags = defaultfilters.removetags = stringfilter(removetags);

var striptags =
    defaultfilters.striptags =
    function(value) {

    return value.replace(/<[^>]*?>/g, "");
};
striptags.is_safe = true;
striptags = defaultfilters.striptags = stringfilter(striptags);

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
register.filter("escape", escape_);
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
register.filter("striptags", striptags);
register.filter("wordcount", wordcount);
register.filter("ljust", ljust);
register.filter("rjust", rjust);
register.filter("center", center);
register.filter("force_escape", force_escape);
register.filter("safe", safe);

//helpers
var escape_pattern = function(pattern) {    return pattern.replace(/([^A-Za-z0-9])/g, "\\$1");};


return defaultfilters;
