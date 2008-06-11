
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
