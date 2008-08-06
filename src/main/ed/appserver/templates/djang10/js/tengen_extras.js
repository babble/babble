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

var tengen_extras =
    djang10.tengen_extras = 
    {};

var log = log.djang10.tengen_extras;

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
        context.__set_root(this.var_name, this.filter_expr.resolve(context));
    }
};

var GlobalsControlNode =
    tengen_extras.GlobalsControlNode =
    function(setting, nodelist) {

    this.setting = setting;
    this.nodelist = nodelist;
};
GlobalsControlNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<GlobalsControlNode: " + this.setting + ">";
    },
    __render: function(context, printer) {
        var has_setting = "__use_globals" in context;
        var old_setting = context["__use_globals"];
        context["__use_globals"] = this.setting;
        this.nodelist.__render(context, printer);
        context["__use_globals"] = old_setting;
    }
};

var do_setNode =
    tengen_extras.do_setNode =
    function(parser, token) {

    log.error("The Set tag has been deprecated please use the standard with tag.  This tag will modify the context object!");
    
    var pattern = /^\s*\S+\s+(\S*)\s*=\s*(.+?)\s*\;?$/;
    var matches = pattern.exec(token.contents);
    
    if(matches.length != 3)
        throw "set expects te following format: {% set varName = expression|filter1|filter2 %}. " + token.contents;

    var var_name = matches[1];
    var filter_expr = parser.compile_filter(matches[2]);
    
    return new SetNode(var_name, filter_expr);
};

register.tag("set", do_setNode);

var globals_ =
    tengen_extras.globals_ =
    function(parser, token) {
    
    var args = token.contents.split(/\s+/);
    if(args.length != 2)
        throw djang10.NewTemplateException("'globals' tag requires exactly one argument.");

    var arg = args[1];
    if(! ["on", "off"].contains(arg))
        throw djang10.NewTemplateException("'"+args[0]+"' argument should be 'on' or 'off'");
    
    var nodelist = parser.parse(["end"+args[0]]);
    parser.delete_first_token();
    
    return new GlobalsControlNode(arg == "on", nodelist);
};
register.tag("globals", globals_);

return tengen_extras;
