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

var force_string = 
    function(obj) {

    if (obj != null && !(obj instanceof String))
        obj = obj.toString();

    return obj;
};

var stringfilter =
    defaultfilters.stringfilter =
    function(func) {

    var f = function() {
        arguments[0] = force_string(arguments[0]);

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
var wordwrap =
    defaultfilters.wordwrap =
    function(value, arg) {
    
    var width = parseInt(arg);
    var words = value.split(" ");
    var word = value[0];
    var pos = word.length - word.lastIndexOf("\n") - 1;
    
    var results = "";
    
    results += word;
    for(var i=1; i<words.length; i++) {
        word = words[i];
        var lines = word.split("\n");
        
        pos += lines[0].length + 1
        if(pos > width) {
            results += "\n";
            pos = lines[lines.length - 1].length;
        }
        else {
            results += " ";
            if(lines.length > 1)
                pos = lines[lines.length - 1].length;
        }
        results += word;
    }
    return results;
};
wordwrap.is_safe = true;
wordwrap = defaultfilters.wordwrap = stringfilter(wordwrap);

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



///////////////////////
// LISTS             //
///////////////////////


var dictsort =
    defaultfilters.dictsort =
    function(value, arg) {

    return value.slice().sort(function(a, b) {
        var val_a = a[arg];
        var val_b = b[arg];
         
        return (val_a < val_b)? -1 : (val_a == val_b)? 0 : 1;
    });
};
dictsort.is_safe = false;

var dictsortreversed =
    defaultfilters.dictsortreversed =
    function(value, arg) {

    return dictsort(value, arg).reverse();
};
dictsortreversed.is_safe = false;

var first =
    defaultfilters.first =
    function(value) {

    return (value.length > 0)? value[0] : "";
};
first.is_safe = false;

var join =
    defaultfilters.join =
    function(value, arg) {

    var data = value.map(force_string).join(arg);
    var safe_args = value.reduce(function(lhs, rhs) { return lhs && djang10.is_safe(rhs);}, true);

    if(safe_args)
        data = djang10.mark_safe(data);

    return data;        
};
join.is_safe = true;

var last =
    defaultfilters.last =
    function(value) {

    return (value.length > 0)? value[value.length - 1] : "";
};
last.is_safe = true;

var length =
    defaultfilters.length =
    function(value) {

    return value.length;
};
length.is_safe = true;

var length_is =
    defaultfilters.length_is =
    function(value, arg) {

    return value.length == arg;
};
length_is.is_safe = true; 

var random =
    defaultfilters.random =
    function(value) {

    if(value.length <= 0)
        throw "Empty array";

    var index = Math.floor( Math.random() * value.length );

    return value[index];
};
random.is_safe = true;

var slice_ =
    defaultfilters.slice_ =
    function(value, arg) {

    try {
        var bits = arg.split(":");
        var start = (bits[0] == null)? 0 : parseInt(bits[0]);
        var end = (bits[1] == null)? value.length : parseInt(bits[1]);
        var step = (bits[2] == null)? 1 : parseInt(bits[2]);
        
        var result = new Array(Math.ceil( value.length/step ))
        
        for(var i=0; i<result.length; i++)
            result[i] = value[start + (i * step)];
        
        return result;
    } catch(e) {
        return value;
    }
};
slice_.is_safe = true;

var unordered_list =
    defaultfilters.unordered_list =
    function(value, autoescape) {

    autoescape = !!autoescape;
    
    var convert_old_style_list = function(list_) {
        if(!(list_ instanceof Array) || list_.length != 2)
            return [list_, false];
        
        var first_item = list_[0];
        var second_item = list_[1];
        
        if(second_item.length == 0)
            return [[first_item], true];
        var old_style_list = true;
        var new_second_item = [];
        for(var i=0; i<second_item.length; i++) {
            var sublist = second_item[i];
            var temp = convert_old_style_list(sublist);
            var item = temp[0];
            var old_style_list = temp[1];
            
            if(!old_style_list)
                break;
            new_second_item.push.apply(new_second_item, item);
        }
        if(old_style_list)
            second_item = new_second_item;
        return [[first_item, second_item], old_style_list];
    };
    var _helper = function(list_, tabs) {
        if(tabs == null) tabs = 1;
        
        var indent = "";
        for(var i=tabs; i>0; i--)
            indent += "\t";
        
        var output = [];
        var list_length = list_.length;
        var i=0;
        
        while(i < list_length) {
            var title = list_[i];
            var sublist = "";
            var sublist_item = null;
            
            if(title instanceof Array) {
                sublist_item = title;
                title = "";
            }
            else if(i < list_length -1) {
                var next_item = list_[i+1];
                if(next_item && (next_item instanceof Array) && next_item.length > 0) {
                    sublist_item = next_item;
                    i++;
                }
            }
            if(sublist_item && sublist_item.length > 0) {
                sublist = _helper(sublist_item, tabs + 1);
                sublist = "\n"+indent+"<ul>\n"+sublist+"\n"+indent+"</ul>\n" + indent;
            }
            title = force_string(title);
            if(autoescape && !djang10.is_safe(title))
                title = escapeHTML(title);
            output.push(indent + "<li>" + title + sublist + "</li>");
            i++;
        }
        return output.join("\n");
    };

    value = convert_old_style_list(value)[0];
    return djang10.mark_safe(_helper(value))
};
unordered_list.is_safe = true;
unordered_list.needs_autoescape = true;


///////////////////////
// INTEGERS          //
///////////////////////
var add =
    defaultfilters.add =
    function(value, arg) {

    return parseInt(value) + parseInt(arg);
};
add.is_safe = true;

var get_digit =
    defaultfilters.get_digit =
    function(value, arg) {

    arg = parseInt(arg);
    value = parseInt(value);
    if(isNaN(arg) || isNaN(value))
        return value;

    if(arg < 1)
        return value;

    var value_str = value.toString();
    if(arg >= value_str.length)
        return 0;
    
    return parseInt(value_str[value_str.length - arg]);
};
get_digit.is_safe = true;



///////////////////////
// DATE              //
///////////////////////


var date =
    defaultfilters.date =
    function(value, arg) {

    if(!value)
        return "";
    
    //TODO: if arg is null, use django.DATE_FORMAT

    return djang10.formatDate(value, arg);        
};
date.is_safe = true;

//TODO: time

var _time_since = function(d, now) {
    //TODO: implement and use the actual translation system
    var ungettext = function(singular, plural, n) { return ((n == 1) || (!plural))? singular : plural; };
    var chunks = [
      [60 * 60 * 24 * 365, function(n) { return ungettext('year', 'years', n); }],
      [60 * 60 * 24 * 30, function(n) { return ungettext('month', 'months', n); }],
      [60 * 60 * 24 * 7, function(n) { return ungettext('week', 'weeks', n); }],
      [60 * 60 * 24, function(n) { return ungettext('day', 'days', n); }],
      [60 * 60, function(n) { return ungettext('hour', 'hours', n); }],
      [60, function(n) { return ungettext('minute', 'minutes', n); }]
    ];
    
    var now_ms = (now || new Date()).getTime();
    var then_ms = d.getTime();
    var diff_secs = (now_ms - then_ms)/1000;
    
    if(diff_secs <= 0) return "0 " + ungettext("minutes");
    
    var seconds;
    var name_func;
    var count;
    var i;
    for(i=0; i<chunks.length; i++) {
        seconds = chunks[i][0];
        name_func = chunks[i][1];
        count = Math.floor(diff_secs/seconds);
        
        if(count != 0)
            break;
    }
    var s = count + " " + name_func(count);
    
    if(i + 1 < chunks.length) {
        var seconds2 = chunks[i+1][0];
        var name_func2 = chunks[i+1][1];
        var count2 = Math.floor( (diff_secs - (seconds * count)) / seconds2 );
        
        if(count2 != 0)
            s += ", " + count2 + " " + name_func2(count2);
    }
    return s;
};
var timesince =
    defaultfilters.timesince =
    function(value, arg) {

    if(!value)
        return "";
    
    return (arg)? _time_since(arg, value) : _time_since(value);
};

var timeuntil =
    defaultfilters.timeuntil =
    function(value, arg) {

    if(!value)
        return "";
    
    return (arg)? _time_since(arg, value) : _time_since(new Date(), value);
};


///////////////////////
//LOGIC              //
///////////////////////


var default_ =
    defaultfilters.default_ =
    function(value, arg) {
        
    return (djang10.Expression.is_true(value))? value : arg;
};
default_.is_safe = false;

var default_if_none =
    defaultfilters.default_if_none =
    function(value, arg) {
        
    return (value == null)? arg : value;
};
default_if_none.is_safe = false;


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
register.filter("first", first);
register.filter("last", last);
register.filter("random", random);
register.filter("slice", slice_);
register.filter("unordered_list", unordered_list);
register.filter("add", add);
register.filter("get_digit", get_digit);
register.filter("timesince", timesince);
register.filter("timeuntil", timeuntil);
register.filter("wordwrap", wordwrap)

//helpers
var escape_pattern = function(pattern) {    return pattern.replace(/([^A-Za-z0-9])/g, "\\$1");};


return defaultfilters;
