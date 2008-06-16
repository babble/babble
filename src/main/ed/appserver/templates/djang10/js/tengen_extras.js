var tengen_extras =
    djang10.tengen_extras = 
    {};

register = new djang10.Library();

var SetNode =
    tengen_extras.SetNode =
    function(var_name, filter_expr) {

    this.var_name = var_name;
    this.filter_expr = filter_expr;
};
SetNode.prototype = {
    __proto__: djang10.Node.prototype,
    toString: function() {
        return "<Set Node: " + this.var_name + " = " + this.filter_expr + ">";
    },
    __render: function(context, printer) {
        context[this.var_name] = this.filter_expr.resolve(context);
    }
};


var do_setNode =
    tengen_extras.do_setNode =
    function(parser, token) {

    var pattern = /^\s*\S+\s+(\S*)\s*=\s*(.+?)\s*\;?$/;
    var matches = pattern.exec(token.contents);
    
    if(matches.length != 3)
        throw "set expects te following format: {% set varName = expression|filter1|filter2 %}. " + token.contents;

    var var_name = matches[1];
    var filter_expr = parser.compile_filter(matches[2]);
    
    return new SetNode(var_name, filter_expr);
};

register.tag("set", do_setNode);


return tengen_extras;