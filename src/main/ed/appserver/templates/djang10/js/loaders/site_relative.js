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

if(!djang10.loaders)
    djang10.loaders = {};

var site_relative =
    djang10.loaders.site_relative =
    {};

var log = log.djang10.loaders.site_relative;

var load_template_source =
    site_relative.load_template_source =
    function(template_name, template_dirs){

    template_name = template_name.trim().replace(/\/+/g, "/");
    if(template_name[0] != "/") {
        if(djang10.DEBUG)
            log("site relative only supports paths that start w. a leading slash: " + template_name );
        
        return null;
    }

    var template = local.getFromPath(template_name);
    if (template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript")
        return template;

    if(djang10.DEBUG)
        log("Failed to find the template: " + template_name + " got: " + template);
    
    return null;
};

return site_relative;
