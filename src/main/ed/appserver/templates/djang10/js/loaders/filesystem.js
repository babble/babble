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

var filesystem =
    djang10.loaders.filesystem =
    {};

var log = log.djang10.loaders.filesystem;

var load_template_source =
    filesystem.load_template_source =
    function(template_name, template_dirs) {

    template_name = template_name.trim();
    if(template_name[0] == "/") {
        log.debug("skipping filesystem loader because path is absolute [" + template_name + "]");
        return null;
    }

    var template_roots = template_dirs || djang10.TEMPLATE_DIRS;
    
    for(var i=template_roots.length-1; i >=0; i--) {
        var template_root = template_roots[i];

        log.debug("checking template root [" + template_root + "]");

        if(template_root instanceof String) {
            try {
                template_root = djang10.resolve_absolute_path(template_root);
            } catch(e) {
                log.warn("failed to resolve the template root [" + template_root + "]. ", e);
                continue;
            }
        }

        if(!(template_root instanceof "ed.appserver.JSFileLibrary")) {
            log.debug("template root: " + template_root + " is not a FileLibrary, got: " + template_root)
            continue;
        }

        var template;
        try {
            template = template_root.getFromPath(template_name);
        } catch(e) {
            log.error("Failed to resolve template ["+template_name+"].", e);
        }
        if (template instanceof "ed.appserver.templates.djang10.Djang10CompiledScript") {
            log.debug("Found [" + template_root + "/" + template_name + "]");
            return template;
        }
        
        log.debug("template root["+template_root+"] doesn't contain the template["+template_name+"]");
    }
    
    log.debug("failed to find the template [" + template_name + "]");
    
    return null;
};

return filesystem;
