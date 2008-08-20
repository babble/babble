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

var absolute =
    djang10.loaders.absolute =
    {};

var log = log.djang10.loaders.absolute;

var load_template_source =
    absolute.load_template_source =
    function(template_name, template_dirs){

    var template = resolve_abs_path(template_name);

    if(template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript") {
        log.debug("Found " + template_name);
        return template;
    }

    log.debug("Failed to find the template: " + template_name + " got: " + template);

    return null;
};

//FIXME: merge w. filesystem
var path_re = new RegExp("^/(.+?)(?:/(.*))?$");

var resolve_abs_path =
    function(path) {

    path = path.trim().replace(/\/+/g,"/");

    if(path[0] != "/") {
        log.debug("No leading slash in: " + path);
        return null;
    }

    var parts = path_re.exec(path);
    
    var root = scope[parts[1]];
    if(!(root instanceof "ed.appserver.JSFileLibrary")) {
        log.debug("Path not rooted in a JSFileLibrary: " + path);
        return null;
    }

    return root.getFromPath(parts[2]);
};

return absolute;
