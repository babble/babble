if(!djang10.loaders)
    djang10.loaders = {};

var absolute =
    djang10.loaders.absolute =
    {};

var load_template_source =
    absolute.load_template_source =
    function(template_name, template_dirs){

    if (template_name[0].trim() != "/")
        throw "Template not found";

    var template = resolve_abs_path(template_name);

    if(template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript")
        return template;

    throw "Template not found";
};

//FIXME: merge w. filesystem
var path_re = new RegExp("^/(.+?)(?:/(.*))?$");

var resolve_abs_path =
    function(path) {

    path = path.trim().replace(/\/+/g,"/");

    if(path[0] != "/")
        throw "Path not found";

    var parts = path_re.exec(path);
    
    var root = scope[parts[1]];
    if(!(root instanceof "ed.appserver.JSFileLibrary"))
        throw "Path not found";

    return (parts[2])? root.getFromPath(parts[2]) : root;
};

return absolute;
