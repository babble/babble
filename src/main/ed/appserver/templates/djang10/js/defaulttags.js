var defaulttags =
    djang10.defaulttags = 
    {};

register = new djang10.Library();



var CommentNode =
    defaulttags.CommentNode =
    function() {
};

CommentNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Comment Node>"
    },
    __render: function(context, printer) {
        //noop
    }
};


var CycleNode =
    defaulttags.CycleNode =
    function(cyclevars, variable_name) {
        
    this.cyclevars = cyclevars;
    this.i = 0;
    this.variable_name = variable_name;
};
CycleNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {        
        return "<Cycle Node: cycleVars: " + this.cyclevars + ", name: " + this.variable_name + ">"; 
    },
    __render: function(context, printer) {
        var value = this.cyclevars[this.i].resolve(context);

        if(++this.i >= this.cyclevars.length)
            this.i = 0;
            
        if(this.variable_name)
            context[this.variable_name] = value;
        
        printer(value);
    }
};

var FilterNode =
    defaulttags.FilterNode =
    function(filter_expr, nodelist) {
        
        this.filter_expr = filter_expr;
        this.nodelist = nodelist;
};
FilterNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Filter Node: " + filter_expr + ">";
    },
    
    __render: function(context, printer) {
        context.push();
        var output = this.nodelist.render(context);
        context["temp"] = output;
        var filtered = this.filter_expr.resolve(context);
        context.pop();
        
        printer(filtered);
    }
};

var FirstOfNode =
    defaulttags.FirstOfNode =
    function(exprs) {
        
    this.exprs = exprs;
};
FirstOfNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<FirstOf Node: " + this.exprs.join(",").substring(0, 25) + "...>";
    },
    
    __render: function(context, printer) {
        for(var i=0; i<this.exprs.length; i++) {
            var expr = this.exprs[i];
            var value = expr.resolve(context);
            if(djang10.Expression.is_true(value))
                printer(value);
        }
    }
}

var ForNode =
    defaulttags.ForNode =
    function(loopvar, sequence, is_reversed, nodelist_loop) {
    
    this.loopvar = loopvar;
    this.sequence = sequence;
    this.is_reversed = is_reversed;
    this.nodelist_loop = nodelist_loop;
    
};

ForNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        var rev_text = this.is_reversed? " reversed" : "";
        return "<For Node: for " + this.loopvar 
            + " in " + this.sequence 
            + ", tail_len: " + this.nodelist_loop.length 
            + rev_text + ">"; 
    },
    get_nodes_by_type: function(constructor) {
        var nodes = [];
        if(isinstance(this, constructor))
            nodes.push(this);
        
        nodes.addAll(this.nodelist_loop.get_nodes_by_type(constructor));
        
        return nodes;
    },
    __render: function(context, printer) {
        var parentloop;
        
        parentloop = ("forloop" in context)? parentloop = context["forloop"] : {};
        context.push();
        
        var values = this.sequence.resolve(context);
        if(values == null || values == djang10.Expression.UNDEFINED_VALUE)
            values = [];
        if(this.is_reversed)
            values.reverse();
        
        var loop_dict = context["forloop"] = {parentloop: parentloop};
        for(var i=0; i<values.length; i++) {
            var item = values[i];
            
            loop_dict['counter0'] = i;
            loop_dict['counter'] = i+1;
            loop_dict['revcounter'] = values.length - i;
            loop_dict['revcounter0'] = values.length - i - 1;
            loop_dict['first'] = (i==0);
            loop_dict['last'] = (i== (values.length -1));
            
            context[this.loopvar] = item;
            
            this.nodelist_loop.__render(context, printer);
        }
        context.pop();
    }
};


var IfEqualNode =
    defaulttags.IfEqualNode =
    function(var1, var2, nodelist_true, nodelist_false, negate) {

    this.var1 = var1;
    this.var2 = var2;
    
    this.nodelist_true = nodelist_true;
    this.nodelist_false = nodelist_false;
    this.negate = negate;
};
IfEqualNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<IfEqualNode>";
    },
    
    __render: function(context, printer) {
        var value1 = this.var1.resolve(context);
        var value2 = this.var2.resolve(context);
        
        if(value1 == djang10.Expression.UNDEFINED_VALUE)
            value1 = null;
        if(value2 == djang10.Expression.UNDEFINED_VALUE)
            value2 = null;
        
        if(this.negate != (value1 == value2)) {
            this.nodelist_true.__render(context, printer);
        }
        else {
            this.nodelist_false.__render(context, printer);
        }
    }
};


var IfNode =
    defaulttags.IfNode =
    function(bool_exprs, nodelist_true, nodelist_false, link_type) {

    this.bool_exprs = bool_exprs;
    this.nodelist_true = nodelist_true;
    this.nodelist_false = nodelist_false;
    this.link_type = link_type;
};
IfNode.LinkTypes = {
    and_: 0,
    or_: 1
};
IfNode.BoolExpr = function(ifnot, bool_expr) {
    this.ifnot = ifnot;
    this.bool_expr = bool_expr;
};
IfNode.BoolExpr.prototype = {
    toString: function() {
        return (this.ifnot?"not ": "") + this.bool_expr;
    }
};
IfNode.prototype = {
    __proto: djang10.Node.prototype,
    
    toString: function() {
        return "<If node: " + this.bool_exprs + ">";
    },
    
    get_nodes_by_type: function(constructor) {
        var nodes = [];
        if(isinstance(this, constructor))
            nodes.push(this);
        
        nodes.addAll(this.nodelist_true.get_nodes_by_type(constructor));
        nodes.addAll(this.nodelist_false.get_nodes_by_type(constructor));
        
        return nodes;
    },
    
    __render: function(context, printer) {
        if(this.link_type == IfNode.LinkTypes.or_) {

            for(var i=0; i<this.bool_exprs.length; i++) {
                var bool_expr = this.bool_exprs[i];

                var value = bool_expr.bool_expr.resolve(context);
                if(djang10.Expression.is_true(value) != bool_expr.ifnot)
                    return this.nodelist_true.__render(context, printer);
                
                return this.nodelist_false.__render(context, printer);
            }
        }
        else {
            for(var i=0; i<this.bool_exprs.length; i++) {
                var bool_expr = this.bool_exprs[i];

                var value = bool_expr.bool_expr.resolve(context);
                if(djang10.Expression.is_true(value) != bool_expr.ifnot)
                    return this.nodelist_false.__render(context, printer);
                
                return this.nodelist_true.__render(context, printer);
            };
        }
    }
};


var LoadNode =
    defaulttags.LoadNode =
    function() {

};
LoadNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Load Node>";
    },
    __render: function(context, printer) {
    }
};

//Registration
var comment =
    defaulttags.comment =
    function(parser, token) {
        
    parser.skip_past("endcomment");
    return new CommentNode();
};
register.tag("comment", comment);

var cycle =
    defaulttags.cycle =
    function(parser, token) {
 
    var args = token.split_contents();
    
    if(args.length < 2)
        throw "'cycle' tag requires at least two arguments";


    
    if(args[1].indexOf(',') > -1) {
        var parts = args[1].split(",").map(quote)
        
        args.splice.apply(args, [1,1].concat(parts));
    }
  
    if(args.length == 2) {
        var name = args[1];

        if(!("_namedCycleNodes" in parser))
            throw "No named cycles in template. '" + name + "' is not defined";
        return parser["_namedCycleNodes"][name];
    }

    if (args.length > 4 && args[args.length - 2] == "as") {
        var name = args[args.length - 1];


        var cycle_exprs = args.slice(1, -2).map(function(item) { return parser.compile_expression(item); });

        var node = new CycleNode(cycle_exprs, name);
        if (!("_namedCycleNodes" in parser)) 
            parser["_namedCycleNodes"] = {};
        
        parser["_namedCycleNodes"][name] = node;
        return node;
    }
    else {
        var cycle_exprs = args.slice(1).map(function(item) { return  parser.compile_expression(item); });
        var node = new CycleNode(cycle_exprs);
        return node;
    }
}
register.tag("cycle", cycle);


var do_filter =
    defaulttags.do_filter =
    function(parser, token) {
        
    var rest = token.contents.replace(/^\S+\s+/, "");
    var filter_expr = parser.compile_filter("temp|" + rest);
    var nodelist = parser.parse(["endfilter"]);
    parser.delete_first_token();
    return new FilterNode(filter_expr, nodelist);
};
register.tag("filter", do_filter);

var do_for =
    defaulttags.do_for =
    function(parser, token) {
        
    var bits = token.split_contents();
    if(bits.length < 4)
        throw "'for' statements should have at least four words: " + token.contents;
    
    var loopvar = bits[1];
    
    var is_reversed = (bits[bits.length - 1] == "reversed");
    if(bits[2] != "in")
        throw "'for' statements should use the format 'for x in y': " + token.contents;
    
    var sequenceStr = bits.slice(3, is_reversed? -1:null).join(" ");
    var sequence = parser.compile_filter(sequenceStr);
;    
    var nodelist_loop = parser.parse(["endfor"]);
    parser.delete_first_token();
    
    return new ForNode(loopvar, sequence, is_reversed, nodelist_loop);
};
register.tag("for", do_for);

var do_ifequal =
    defaulttags.do_ifequal =
    function(parser, token, negate) {
        
    var bits = token.split_contents();
    if(bits.length != 3)
        throw bits[0] + " takes two arguments";

    var var1 = parser.compile_expression(bits[1]);
    var var2 = parser.compile_expression(bits[2]);
    
    var end_tag = "end" + bits[0];
    var nodelist_true = parser.parse(["else", "end"+bits[0]]);
    var nodelist_false;
    if(parser.next_token().contents == "else") {
        nodelist_false = parser.parse(["end"+bits[0]]);
        parser.delete_first_token();
    }
    else {
        nodelist_false = parser.create_nodelist();
    }

    return new IfEqualNode(var1, var2, nodelist_true, nodelist_false, negate);
};
var ifequal =
    defaulttags.ifequal = 
    function(parser, token) {

    return do_ifequal(parser, token, false);    
};
register.tag("ifequal", ifequal);

var ifnotequal =
    defaulttags.ifnotequal =
    function(parser, token) {

    return do_ifequal(parser, token, true);        
};
register.tag("ifnotequal", ifnotequal);
  


var firstof =
    defaulttags.firstof =
    function(parser, token) {

    var bits = token.split_contents();
    if(bits.length < 1)
        throw "'firstof' statement requires at least one argument";
    
    var exprs = bits.map(function(bit) { return parser.compile_expression(bit); });
    return new FirstOfNode(exprs);
};
register.tag("firstof", firstof);

var do_if =
    defaulttags.do_if =
    function(parser, token) {
        
    var bits = token.contents.split(/\s+/);
    bits.shift();
    if(bits.length == 0)
        throw "'if' statement requires at least one argument";
    
    var bitstr = "" + bits.join(" ");
    var boolpairs = bitstr.split(" and ");

    var boolvars = [];
    var link_type;
    if(boolpairs.length == 1) {
        link_type = IfNode.LinkTypes.or_;
        boolpairs = bitstr.split(" or ");
    }
    else {
        link_type = IfNode.LinkTypes.and_;
        if(bitstr.indexOf(" or ") > -1)
        throw "'if' tags can't mix 'and' and 'or'";
    }
    
    for(var i=0; i<boolpairs.length; i++) {
        var boolpair = boolpairs[i];

        if( boolpair.indexOf(" ") > -1) {
            var boolpair_parts = boolpair.split(" ");
            var not = boolpair_parts[0];
            var boolvar = boolpair_parts[1];
            
            if(not != 'not')
                throw "Expected 'not' in if statement";
            boolvars.push(new IfNode.BoolExpr(true, parser.compile_filter(boolvar)));
        }
        else {
            boolvars.push(new IfNode.BoolExpr(false, parser.compile_filter(boolpair)));
        }
    }

    var nodelist_true = parser.parse(["else", "endif"]);
    var nodelist_false;
    var token = parser.next_token();
    if(token.contents == "else") {
        nodelist_false = parser.parse(["endif"]);
        parser.delete_first_token();
    }
    else {
        nodelist_false = parser.create_nodelist();
        parser.delete_first_token();
    }
    
    return new IfNode(boolvars, nodelist_true, nodelist_false, link_type);
};
register.tag("if", do_if);

var load =
    defaulttags.load =
    function(parser, token) {

    var bits = token.split_contents();
    for(var i=1; i<bits.length; i++) {
        var library_file = djang10.loadLibrary(bits[i]);

        var evalScope = scope.child();
        evalScope.setGlobal(true);
        evalScope.eval("library_file()");
        var library = evalScope.get("register");
        
        parser.add_library(library_file["_jxpSource"], library);
    }
    return new LoadNode(library_files);
};
register.tag("load", load);


//private helpers
var quote = function(str) { return '"' + str + '"';};

var isinstance = function(object, constructor){
    while (object != null && (typeof object == "object")) {
        if (object == constructor.prototype) 
            return true;
        object = object.__proto__;
    }
    return false;
};

return defaulttags;
