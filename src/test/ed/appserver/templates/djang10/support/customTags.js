
register = new djang10.Library();

register.filter("moo", function() {
    return "moo";
});

var MyTagNode = function() { };
MyTagNode.prototype = {
    __proto__: djang10.Node.prototype,
    __render: function(context, printer) {
        printer("moo");
    }
}

register.tag("myTag", function(parser, token) {
    return new MyTagNode();
});


var MyTagWithChildNode = function(children) {
    this.children = children;
};
MyTagWithChildNode.prototype = {
    __proto__: djang10.Node.prototype,
    __render: function(context, printer) {
        printer("before_" + this.children.render(context) + "_after");
    }
};

register.tag("myTagWithChild", function(parser, token) {
    var children = parser.parse(["endmyTagWithChild"]);
    parser.delete_first_token();
    
    return new MyTagWithChildNode(children);
});
