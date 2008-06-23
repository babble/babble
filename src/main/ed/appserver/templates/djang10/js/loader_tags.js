var loader_tags =
    djang10.loader_tags = 
    {};

register = new djang10.Library();


var BlockNode =
    loader_tags.BlockNode =
    function(name, nodelist) {
        
    this.name = name;
    this.nodelist = nodelist;
};
BlockNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Block Node: " + this.name + ". Contents: " + this.nodelist + ">";
    },
    
    __render: function(context, printer) {
        var child_blocks = context["__child_blocks"] || [];
        
        var block_to_render = this;
        for(var i=0; i<child_blocks.length; i++) {
            var block_map = child_blocks[i];
            if(block_map[this.name] != null) {
                block_to_render = block_map[this.name];
                break;
            }
        }

        block_to_render.nodelist.__render(context, printer);
    }
};


var ExtendsNode =
    loader_tags.ExtendsNode =
    function(nodelist, parent_name_expr) {

    this.nodelist = nodelist;
    this.parent_name_expr = parent_name_expr;     
};
ExtendsNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Extends Node: extends: '" + this.parent_name_expr +"'>";
    },
    __render: function(context, printer) {
        var parent_name = this.parent_name_expr.resolve(context);
        var parent = djang10.loadTemplate(parent_name);

        var my_blocks = {}; 
        this.nodelist.get_nodes_by_type(BlockNode).each(function(node) {
            my_blocks[node.name] = node;
        });
        
        context.push();
        var child_blocks = context["__child_blocks"];
        if(child_blocks == null)
            child_blocks = context["__child_blocks"] = [];
        else
            child_blocks = context["__child_blocks"] = context["__child_blocks"].slice();
 
        child_blocks.push(my_blocks);

        parent.nodelist.__render(context, printer);
        context.pop();
    }
};


//FIXME: add error handling & library loading
var IncludeNode =
    loader_tags.IncludeNode =
    function(templatePathExpr) {

    this.templatePathExpr = templatePathExpr;        
};
IncludeNode.prototype = {
    __proto__: djang10.Node.prototype,
    
    toString: function() {
        return "<Include Node: '" + this.templatePathExpr + "'>";
    },
    
    __render: function(context, printer) {
        var templateName = this.templatePathExpr.resolve(context);
        var template = djang10.loadTemplate(templateName);
        
        template.nodelist.__render(context, printer);
    }
};

var do_block =
    loader_tags.do_block =
    function(parser, token) {

    var bits = token.contents.split(/\s+/);
    if(bits.length != 2)
        throw  "'" +bits[0] + "' tag takes only one argument";
    
    var block_name = bits[1];
    var loaded_blocks = parser["__loaded_blocks"];
    if(loaded_blocks == null)
        loaded_blocks = parser["__loaded_blocks"] = {};
    
    if(loaded_blocks[block_name] != null)
        throw "'"+bits[0]+"' tag with name '"+block_name+"' appears more than once";
    
    
    var nodelist = parser.parse(["endblock", "endblock " + block_name]);
    parser.delete_first_token();
    
    var block_node = new BlockNode(block_name, nodelist, null);
    loaded_blocks[block_name] = block_node;
    
    return block_node;
};
register.tag("block", do_block);


var do_extends =
    loader_tags.do_extends =
    function(parser, token) {

    var bits = token.split_contents();
    if(bits.length != 2)
        throw "'"+bits[0]+"' takes one argument";
   
    var parent_name_expr = parser.compile_filter(bits[1]);
    if(parent_name_expr.is_literal()) {
        var parent = djang10.loadTemplate(parent_name_expr.get_literal_value());
        var parent_libs = parent.loadedLibraries;
        if(parent_libs !=null) {

            for(var i=0; i<parent_libs.length; i++) {
                parser.add_library(parent_libs[i], false);
            }
        }
    }
    
    var nodelist = parser.parse();
    if(nodelist.get_nodes_by_type(ExtendsNode).length > 0)
        throw "'"+bits[0]+"' cannot appear more than once in the same template";
    
    return new ExtendsNode(nodelist, parent_name_expr);
};
register.tag("extends", do_extends);


var do_include =
    loader_tags.do_include =
    function(parser, token) {


    var args = token.contents.replace(/^\s*\S+\s+/, "");
    var pathExpr = parser.compile_filter(args);
    
    return new IncludeNode(pathExpr);
};
register.tag("include", do_include);


var isinstance = function(object, constructor){
    while (object != null && (typeof object == "object")) {
        if (object == constructor.prototype) 
            return true;
        object = object.__proto__;
    }
    return false;
};

return loader_tags;