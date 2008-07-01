if(!djang10.loaders)
    djang10.loaders = {};

var site_relative =
    djang10.loaders.site_relative =
    {};

var load_template_source =
    site_relative.load_template_source =
    function(template_name, template_dirs){

    template_name = template_name.trim().replace(/\/+/g, "/");
    if(template_name[0] != "/")
        throw "Template not found";

    var template = local.getFromPath(template_name);
    if (template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript")
        return template;

    throw "Template not found";
};

return site_relative;
