var defaultfilters =
    djang10.defaultfilters = 
    {};

register = new djang10.Library();

var lower =
    defaultfilters.lower =
    function(value) {

    return value.toLowerCase();
};

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
    function(value) {

    value = value.replace(/\r\n|\r|\n/g, "\n");
    var paras = value.split(/\n{2,}/);
    for(var i=0; i < paras.length; i++)
        paras[i] = "<p>" + paras[i].trim().replace(/\n/g, "<br />") + "</p>";
        
    return paras.join("\n\n");
};

var linebreaksbr =
    defaultfilters.linebreaks =
    function(value) {

    return value.replace(/\n/g, "<br />");
};

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

//helpers
var escape_pattern = function(pattern) {    return pattern.replace(/([^A-Za-z0-9])/g, "\\$1");};


return defaultfilters;