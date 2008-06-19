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

var urlencode =
    defaultfilters.urlencode =
    function(value) {

    return scope.getParent().getParent().getParent().getParent().escape(value);      
};

var escapeFilter =
    defaultfilters.escapeFilter =
    function(value) {
    
    return escapeHTML(value);
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
        
    return (value == djang10.Expression.UNDEFINED_VALUE)? arg : value;
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

return defaultfilters;