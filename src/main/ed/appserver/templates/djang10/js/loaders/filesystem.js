if(!djang10.loaders)
    djang10.loaders = {};

var filesystem =
    djang10.loaders.filesystem =
    {};

var load_template_source =
    filesystem.load_template_source =
    function(template_name, template_dirs) {

    template_name = template_name.trim();
    if(template_name[0] == "/")
        throw "Template not found";

    var template_roots = template_dirs || djang10.TEMPLATE_DIRS;
    
    for(var i=template_roots.length-1; i >=0; i--) {
        var template_root = template_roots[i];

        
        try {
            if(template_root instanceof String)
                template_root = resolve_abs_path(template_root);
        }catch(e) {
            template_root = null;
        }

        if(!(template_root instanceof "ed.appserver.JSFileLibrary"))
            continue;

        var template = template_root[template_name];
        if (template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript")
            return template;
    }
    
    throw "Template not found";
};

//FIXME: merge w. asbolute
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
