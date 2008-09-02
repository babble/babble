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

var defaulttags =
    djang10.defaulttags = 
    {};

register = new djang10.Library();


var AutoEscapeControlNode =
    defaulttags.AutoEscapeControlNode =
    function(setting, nodelist) {

    this.setting = setting;
    this.nodelist = nodelist;
};
AutoEscapeControlNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<AutoEscapeControlNode: " + this.setting + ">";
    },
    __render: function(context, printer) {
        var has_setting = "autoescape" in context;
        var old_setting = context.autoescape;
        context.autoescape = this.setting;
        this.nodelist.__render(context, printer);
        if(has_setting)
            context.autoescape = old_setting;
    }
};

var CommentNode =
    defaulttags.CommentNode =
    function() {
};

CommentNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Comment Node>";
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
        var template_vars = context["__render_vars"];
        if(template_vars == null)
            template_vars = context["__render_vars"] = new Map();
        
        var i = template_vars.get(this) || 0;

        
        var value = this.cyclevars[i].resolve(context);

        if(++i >= this.cyclevars.length)
            i = 0;
        template_vars.set(this, i);

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
        return "<Filter Node: " + this.filter_expr + ">";
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
            if(djang10.Expression.is_true(value)) {
                printer(value);
                return;
            }
        }
    }
}

var ForNode =
    defaulttags.ForNode =
    function(loopvars, sequence, is_reversed, nodelist_loop) {
    
    this.loopvars = loopvars;
    this.sequence = sequence;
    this.is_reversed = is_reversed;
    this.nodelist_loop = nodelist_loop;
    
};

ForNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        var rev_text = this.is_reversed? " reversed" : "";
        return "<For Node: for " + tojson(this.loopvars) 
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

        if(!djang10.Expression.is_true(values))
            values = [];

        var next_fn;
        var count;
        
        if ( values.getClass().getName() == "ed.db.DBCursor" ){
            if(this.is_reversed) {
                throw "Can't reverse a DBCursor, use array mode, if you need to do this";
            }
            next_fn = function() {
                return values.next();
            };
            count = values.count();
        }
        else {
            next_fn = function() {
                return  values[next_fn.i++];
            };
            next_fn.i=0;

            count = values.length;
        }

        if( this.is_reversed )
            values.reverse();
        
        var loop_dict = context["forloop"] = {parentloop: parentloop};
        for(var i=0; i<count; i++) {
            var item = next_fn();
            
            loop_dict['counter0'] = i;
            loop_dict['counter'] = i+1;
            loop_dict['revcounter'] = count - i;
            loop_dict['revcounter0'] = count - i - 1;
            loop_dict['first'] = (i==0);
            loop_dict['last'] = (i== (count - 1));
            
            if(this.loopvars.length == 1)
                context[this.loopvars[0]] = item;
            else
                for(var j=0;j<this.loopvars.length; j++)
                    context[this.loopvars[j]] = item[j];
            
            this.nodelist_loop.__render(context, printer);
        }
        context.pop();
    }
};

var IfChangedNode =
    defaulttags.IfChangedNode =
    function(nodelist, exprs) {

    this.nodelist = nodelist;
    this._varlist = exprs;    
};
IfChangedNode.are_equal = function(a, b){
    if(typeof(a) != typeof(b))
        return false;

    if(typeof(a) != "object" || typeof(b) != "object")
        return a == b;

    if((a instanceof Array) && (b instanceof Array)) {
        if(a.length != b.length) return false;
        for(var i=0; i<a.length; i++)
            if(!IfChangedNode.are_equal(a[i], b[i]))
                return false;
        return true; 
    }
        
    if(a.equals instanceof Function)
        return a.equals(b);
    
    if(b.equals instanceof Function)
        return b.equals(a);
    
    return a == b;
};
IfChangedNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<IfChanged Node: " + tojson(this.exprs) + ">";
    },
    __render: function(context, printer) {
        var template_vars = context["__render_vars"];
        if(template_vars == null)
            template_vars = context["__render_vars"] = new Map();

        var last_seen = (("forloop" in context) && context.forloop.first)? null : template_vars.get(this);
        
        var compare_to;
        var is_same;
        if(this._varlist.length > 0)
            compare_to = this._varlist.map(function(expr) { return expr.resolve(context); });
        else
            compare_to = this.nodelist.render(context);
            
        if(!IfChangedNode.are_equal(last_seen, compare_to)) {
            var firstloop = (last_seen == null);
            last_seen = compare_to;
            template_vars.set(this, last_seen);
            
            context.push();
            context["ifchanged"] = {"firstloop": firstloop};
            this.nodelist.__render(context, printer);
            context.pop();
        }
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
        
        if(this.negate != IfChangedNode.are_equal(value1, value2)) {
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
            }
            return this.nodelist_false.__render(context, printer);
        }
        else {
            for(var i=0; i<this.bool_exprs.length; i++) {
                var bool_expr = this.bool_exprs[i];
                var value = bool_expr.bool_expr.resolve(context);
                if(djang10.Expression.is_true(value) == bool_expr.ifnot)
                    return this.nodelist_false.__render(context, printer);
            }
            return this.nodelist_true.__render(context, printer);
        }
    }
};

var RegroupNode =
    defaulttags.RegroupNode =
    function(the_list, prop_name, var_name) {

    this.the_list = the_list;
    this.prop_name = prop_name;
    this.var_name = var_name;
};
RegroupNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Regroup Node: " + this.the_list + " by " + this.prop_name + " as " + this.var_name +">";
    },
    __render: function(context, printer) {
        var obj_list = this.the_list.resolve(context);
        if(!djang10.Expression.is_true(obj_list)) {
            context[this.var_name] = [];
            return;
        }
        
        var grouped = [];
        var group = null;
        
        var prop_name = this.prop_name;
        
        if(prop_name) {
            obj_list.each(function(item){
                if (group == null || group.grouper != item[prop_name]) {
                    group = {
                        grouper: item[prop_name],
                        list: []
                    };
                    grouped.push(group);
                }
                group.list.push(item);
            });
        }
        context[this.var_name] = grouped;
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

var NowNode =
    defaulttags.NodeNode =
    function(format_expr) {

    this.format_expr = format_expr;
};
NowNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Now Node: " + this.format_Expr + ">";
    },
    __render: function(context, printer) {
        var format = this.format_expr.resolve(context);
        var formatted_date = djang10.formatDate(new Date(), format);
        printer(formatted_date);
    }
};

var SpacelessNode =
    defaulttags.SpacelessNode =
    function(nodelist) {

    this.nodelist = nodelist;
};
SpacelessNode.prototype ={
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Spaceless Node>";
    },
    __render: function(context, printer) {
        var content = this.nodelist.render(context);
        content = content.trim().replace(/>\s+</g, "><");
        printer(content);
    }
};

var TemplateTagNode =
    defaulttags.TemplateTagNode =
    function(tagtype) {

    this.tagtype = tagtype;
};
//FIXME: these need to be configurable in the parser, and this should be read from the parser
TemplateTagNode.mapping = {
    'openblock': "{%",
    'closeblock': "%}",
    'openvariable': "{{",
    'closevariable': "}}",
    'openbrace': "{",
    'closebrace': "}",
    'opencomment': "{#",
    'closecomment': "#}"
};
TemplateTagNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<TemplateTag Node: '" + this.tagtype + "'>"
    },
    __render: function(context, printer) {
        printer(TemplateTagNode.mapping[this.tagtype]);
    }
};

var WidthRatioNode =
    defaulttags.WidthRatioNode =
    function(val_expr, max_expr, max_width) {

    this.val_expr = val_expr;
    this.max_expr = max_expr;
    this.max_width = max_width;
};
WidthRatioNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<WidthRatio Node: val_expr: " + this.val_expr + ", max_expr: " + this.max_expr + ", max_width: " + this.max_width + ">";
    },
    __render: function(context, printer) {
        try {
            var value = parseFloat( this.val_expr.resolve(context) );
            var maxvalue = parseFloat( this.max_expr.resolve(context) );
            var ratio = (value/maxvalue) * parseInt(this.max_width);
            
            if(!isNaN(ratio))            
                printer(Math.round(ratio) );
        }
        catch(e) {
            //fail silently
        }
    }
};

var WithNode =
    defaulttags.WithNode =
    function(var_, name, nodelist) {

    this.var_ = var_;
    this.name = name;
    this.nodelist = nodelist;
};
WithNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<WithNode>";
    },
    __render: function(context, printer) {
        var val = this.var_.resolve(context);
        context.push();
        context[this.name] = val;
        this.nodelist.__render(context, printer);
        context.pop();
    }
}

//Registration
//{% autoescape\s+(on)|(off) %} 
//Only literal on or off is supported ...no expressions, variables or quotes
var autoescape =
    defaulttags.autoescape =
    function(parser, token) {

    var args = token.contents.split(/\s+/);
    if(args.length != 2)
        throw new djang10.TemplateSyntaxError("'Autoescape' tag requires exactly one argument.", token);

    var arg = args[1];
    if(! ["on", "off"].contains(arg))
        throw new djang10.TemplateSyntaxError("'"+args[0]+"' argument should be 'on' or 'off'", token);
    
    var nodelist = parser.parse(["end"+args[0]]);
    parser.delete_first_token();
    
    return new AutoEscapeControlNode(arg == "on", nodelist);
};
register.tag("autoescape", autoescape);

//{% comment %}
var comment =
    defaulttags.comment =
    function(parser, token) {
    
    parser.skip_past("endcomment");
    return new CommentNode();
};
register.tag("comment", comment);


//TODO: Needs split contents by expression (spaces & commas)
/*
 * old style definition: params are unquoted strings w/o spaces, delimited y commas
 * {% cycle a,b,c %} => {% cycle "a" "b" "c" %}
 * {% cycle literal1,literal2,literal3 as cycle1 %}  => {% cycle "a" "b" "c" as cycle1 %}
 * 
 * new style definition: params delimited by spaces and either quoted strings or variables
 * {% cycle cycle1 %} 
 * {% cycle var1 "str1" foo.bar %}
 * 
 * {% cycle cycleName %}
 * 
 * expressions with spaces or commas are not supported!!!!:
 *  Need much more sophisticated parsing...
 */
var cycle =
    defaulttags.cycle =
    function(parser, token) {
 
    var args = token.split_contents();
    
    if(args.length < 2)
        throw new djang10.TemplateSyntaxError("'cycle' tag requires at least two arguments", token);


    //backward compatibility: {% cycle a,b %} or {% cycle a,b as foo %}
    if(args[1].indexOf(',') > -1) {
        var parts = args[1].split(",").map(quote)
        
        args.splice.apply(args, [1,1].concat(parts));
    }

    //{% cycle name %} case
    if(args.length == 2) {
        var name = args[1];

        if(!("_namedCycleNodes" in parser))
            throw new djang10.TemplateSyntaxError("No named cycles in template. '" + name + "' is not defined", token);
        return parser["_namedCycleNodes"][name];
    }

    //named cycle definition: {% cycle arg as name %} or {% cycle arg arg2 as name %} case 
    if (args.length > 4 && args[args.length - 2] == "as") {
        var name = args[args.length - 1];


        var cycle_exprs = args.slice(1, -2).map(function(item) { return parser.compile_expression(item); });

        var node = new CycleNode(cycle_exprs, name);
        if (!("_namedCycleNodes" in parser)) 
            parser["_namedCycleNodes"] = {};
        
        parser["_namedCycleNodes"][name] = node;
        return node;
    }
    //anonymous cycle: {% cycle arg arg2 %}
    else {
        var cycle_exprs = args.slice(1).map(function(item) { return  parser.compile_expression(item); });
        var node = new CycleNode(cycle_exprs);
        return node;
    }
}
register.tag("cycle", cycle);

//TODO: debug tag

/*
 * {% filter filterName:ANY_EXPRESSION %}filtered content{% endfilter %}
 * {% filter filterName:ANY_EXPRESSION | filterName2:  ANY_EXPRESSION %}filtered content{% endfilter %}
 */
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


/*
 * {% for var in ANY_FILTER_EXPRESSION %}
 * {% for var in ANY_FILTER_EXPRESSION reversed %}
 */
var do_for =
    defaulttags.do_for =
    function(parser, token) {
        
    var bits = token.split_contents();
    if(bits.length < 4)
        throw new djang10.TemplateSyntaxError("'for' statements should have at least four words: " + token.contents, token);
    
    var is_reversed = (bits[bits.length - 1] == "reversed");
    var in_index = bits.lastIndexOf("in");
    
    if(in_index < 2)
        throw new djang10.TemplateSyntaxError("'for' statements should use the format 'for x in y': " + token.contents, token);
    
    
    
    var loopvars = bits.slice(1, in_index).join(" ").split(",").map(function(bit) { return bit.trim(); } );
    
    if(loopvars.some(function(bit) { return !bit || bit.contains(" "); } ))
        throw new djang10.TemplateSyntaxError("'for' tag received and invalid argument:" + token.contents, token)
    
    var sequenceStr = bits.slice(in_index+1, is_reversed? -1 : bits.length).join(" ");
    var sequence = parser.compile_filter(sequenceStr);
 
    var nodelist_loop = parser.parse(["endfor"]);
    parser.delete_first_token();
    

    return new ForNode(loopvars, sequence, is_reversed, nodelist_loop);
};
register.tag("for", do_for);

//TODO: needs split by expression (spaces)
/*
 * {% ifequal NO_SPACE_EXPRESSION NO_SPACE_EXPRESSION %} 
 */
var do_ifequal =
    defaulttags.do_ifequal =
    function(parser, token, negate) {
        
    var bits = token.split_contents();
    if(bits.length != 3)
        throw new djang10.TemplateSyntaxError(bits[0] + " takes two arguments", token);

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
  

//TODO: spaces in expressions are not supported
/*
 * {% firstof a %}
 * {% firstof "a" %}
 * {% firstof a b c "d" %}
 * {% firstof NO_SPACE_EXPRESSION NO_SPACE_EXPRESSION NO_SPACE_EXPRESSION)
 */
var firstof =
    defaulttags.firstof =
    function(parser, token) {

    var bits = token.split_contents().slice(1);
    if(bits.length < 1)
        throw new djang10.TemplateSyntaxError("'firstof' statement requires at least one argument", token);
    
    var exprs = bits.map(function(bit) { return parser.compile_expression(bit); });
    return new FirstOfNode(exprs);
};
register.tag("firstof", firstof);

//XXX: bitwise or operator will conflict with this
//XXX: can't use variables named or, and & not
/*
 * {% if NO_SPACE_FILTER_EXPRESSION %}
 * {% if NO_SPACE_FILTER_EXPRESSION or not NO_SPACE_FILTER_EXPRESSION or ... %}
 * {% if NO_SPACE_FILTER_EXPRESSION and not NO_SPACE_FILTER_EXPRESSION and ... %} 
 */
var do_if =
    defaulttags.do_if =
    function(parser, token) {
        
    var bits = token.split_contents();
    bits.shift();
    if(bits.length == 0)
        throw new djang10.TemplateSyntaxError("'if' statement requires at least one argument", token);
    
    var bitstr = "" + bits.join(" ");
    var boolpairs = djang10.smart_split(bitstr, [" and "]);

    var boolvars = [];
    var link_type;
    if(boolpairs.length == 1) {
        link_type = IfNode.LinkTypes.or_;
        boolpairs = djang10.smart_split(bitstr, [" or "])
    }
    else {
        link_type = IfNode.LinkTypes.and_;
        if(djang10.smart_split(bitstr, [" or "]).length > 1)
            throw new djang10.TemplateSyntaxError("'if' tags can't mix 'and' and 'or'", token);
    }
    
    for(var i=0; i<boolpairs.length; i++) {
        var boolpair = boolpairs[i];

        
        if( boolpair.indexOf(" ") > -1) {
            var boolpair_parts =  /\s*not\s+(.+)$/.exec(boolpair);
            
            if(boolpair_parts != null)
                boolvars.push(new IfNode.BoolExpr(true, parser.compile_filter(boolpair_parts[1])));
            else
                boolvars.push(new IfNode.BoolExpr(false, parser.compile_filter(boolpair)));
        }
        else {
            boolvars.push(new IfNode.BoolExpr(false, parser.compile_filter(boolpair)));
        }
    }

    var nodelist_true = parser.parse(["else", "endif"]);
    var nodelist_false;
    var token2 = parser.next_token();
    if(token2.contents == "else") {
        nodelist_false = parser.parse(["endif"]);
        parser.delete_first_token();
    }
    else {
        nodelist_false = parser.create_nodelist();
    }
    
    return new IfNode(boolvars, nodelist_true, nodelist_false, link_type);
};
register.tag("if", do_if);

/*
 * {% ifchanged NO_SPACE_EXPRESSION NO_SPACE_EXPRESSION %}
 * {% ifchanged %}
 */
var ifchanged =
    defaulttags.ifchanged =
    function(parser, token) {

    var bits = token.split_contents();
    var nodelist = parser.parse(["endifchanged"]);
    parser.delete_first_token();
    
    var exprs = [];
    for(var i=1; i<bits.length; i++) {
        exprs.push(parser.compile_expression(bits[i]) );
    }
    
    return new IfChangedNode(nodelist, exprs);
};
register.tag("ifchanged", ifchanged);

/*
 * {% load NO_SPACE_LITERAL_PATH %}
 * {% load NO_SPACE_LITERAL_PATH NO_SPACE_LITERAL_PATH  %}
 */
var load =
    defaulttags.load =
    function(parser, token) {

    var bits = token.split_contents();
    for(var i=1; i<bits.length; i++) {
        var library_file = djang10.loadLibrary(bits[i]);
        var library = djang10.evalLibrary(library_file);
        
        parser.add_library(library);
        parser.add_dependency(library_file["_jxpSource"]);
    }
    return new LoadNode();
};
register.tag("load", load);


/*
 * {% now ANY_FILTER_EXPRESSION %}
 */
var now =
    defaulttags.now =
    function(parser, token) {

    var m = /\S+\s+(.+)$/.exec(token.contents);
    
    if(m == null)
        throw new djang10.TemplateSyntaxError("'now' statement takes one argument", token);
    var expr = parser.compile_filter(m[1]);
    
    return new NowNode(expr);
};
register.tag("now", now);

/*
 * {% regroup FILTER_EXPRESSION by LITERAL_NAME as LITERAL_NAME %}
 */
var regroup =
    defaulttags.regroup =
    function(parser, token) {

    var pattern = /^\s*\S+\s+(.+?)\s+by\s+(\S+)\s+as\s+(\S+)\s*$/;
    var match = pattern.exec(token.contents);
    
    if(match == null)
        throw new djang10.TemplateSyntaxError("'regroup' tag requires the format: {% regroup list_expression|optional_filter:with_optional_param by prop_name as result_var_name %}. got: " + token, token);
    
    var list_expr = parser.compile_filter(match[1]);
    var prop_name = match[2];
    var var_name = match[3];
    
    return new RegroupNode(list_expr, prop_name, var_name);
};
register.tag("regroup", regroup);

/*
 * {% spaceless %}
 */
var spaceless =
    defaulttags.spaceless =
    function(parser, token) {
    
    var nodelist = parser.parse(["endspaceless"]);
    parser.delete_first_token();
    return new SpacelessNode(nodelist);
};
register.tag("spaceless", spaceless);

/*
 * {% templatetag openblock %}
 * {% templatetag closeblock %}
 * {% templatetag openvariable %}
 * {% templatetag closevariable %}
 * {% templatetag openbrace %}
 * {% templatetag closebrace %}
 * {% templatetag opencomment %}
 * {% templatetag closecomment %}
 */
var templatetag =
    defaulttags.templatetag =
    function(parser, token) {

    var bits = token.contents.split(/\s+/);

    if(bits.length != 2)
        throw new djang10.TemplateSyntaxError("'templatetag' statement takes one argument", token)
    
    var tag = bits[1];
    if(!(tag in TemplateTagNode.mapping))
        throw new djang10.TemplateSyntaxError("Invalid templatetag argument: '"+tag+"'. Must be one of: " + TemplateTagNode.mapping.keySet().join(", "), token);
    
    return new TemplateTagNode(tag);
};
register.tag("templatetag", templatetag);

//TODO: url tag


//TODO: spaces in expression filter is not supported
/*
 * {% widthratio NO_SPACE_FILTER_EXPR NO_SPACE_FILTER_EXPR LITERAL_INT %}
 */
var widthratio =
    defaulttags.widthratio =
    function(parser, token) {

    var bits = token.split_contents();
    if(bits.length != 4)
        throw new djang10.TemplateSyntaxError("widthratio takes three arguments", token);
    var this_value_expr = bits[1];
    var max_value_expr = bits[2];
    var max_width = bits[3];
    
    try {
        max_width = parseInt(max_width);
    }
    catch(e) {
        throw new djang10.TemplateSyntaxError("widthratio final argument must be an integer", token);
    }
    
    return new WidthRatioNode(parser.compile_filter(this_value_expr), parser.compile_filter(max_value_expr), max_width);
};
register.tag("widthratio", widthratio);

/*
 * {% with ANY_FILTER_EXPR as LITERAL_NAME %}
 */
var do_with =
    defaulttags.do_with =
    function(parser, token) {
    
    var m = /(\S+)\s+(.+)\s+as\s+([\w_\$]+)$/.exec(token.contents);
    
    if(m == null)
        throw new djang10.TemplateSyntaxError("with expected format is 'value as name'", token);
    
    var command = m[1];
    var var_ = parser.compile_filter(m[2]);
    var name = m[3];
    var nodelist = parser.parse([ "end" + command ]);
    parser.delete_first_token();
    
    return new WithNode(var_, name, nodelist);
};
register.tag("with", do_with);

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
