
//register = new core.templates.djang10.Library();
var djang10 = __djang10.publicApi;
register = new djang10.Library();

register.filter("moo", function() {
    return "moo";
});


register.tag("myTag", function(parser, token) {
    return {
        render: function(cxt) {
            return "moo";
        }
    }
});


register.tag("myTagWithChild", function(parser, token) {
    var children = parser.parse(["endmyTagWithChild"]);
    parser.next_token();
    
    return {
        render: function(ctx) {
            return "before_" + children.render(ctx) + "_after";
        }
    }
});
