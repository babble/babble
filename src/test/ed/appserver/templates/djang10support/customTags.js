
//register = new core.templates.djang10.Library();
register = new __djang10.Library();

//register.filter("moo", function() {
register.__filter("moo", function() {
    return "moo";
});
