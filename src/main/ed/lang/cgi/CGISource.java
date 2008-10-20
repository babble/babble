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

package ed.lang.cgi;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jvyamlb.YAML;

import ed.js.JSFunction;
import ed.js.engine.Scope;
import ed.appserver.JSFileLibrary;
import ed.appserver.jxp.JxpSource;
import ed.log.Logger;
import ed.util.Dependency;

/**
 *   POC processor for AE, currently associated with the "yaml"
 *   file type.  This is only a shortcut to get the class created
 *   and it's getFunction() method called.  We need to find a better
 *   way to do this - this could be used more generally for CGI
 */
public class CGISource extends JxpSource {

    final File _file;
    final JSFileLibrary _lib;
    final static Logger _log = Logger.getLogger("CGISource");
    protected final List<MapEntry> _handlerList = new ArrayList<MapEntry>();
    
    public CGISource(File f , JSFileLibrary lib){
        _file = f;
        _lib = lib;

        digestYaml();
    }

    /**
     * Digets the app.yaml file - just keeps the script matches
     *
     * TODO - get the static matches as well so we can let the Appserver handle when appropos
     */
    protected void digestYaml() {
        try {
            // the YAML package simply sucks - it's written by Oli and it's AllJrubyAllTheTime - must dump!

            Map m = (Map) YAML.load(new FileInputStream(_file));

            org.jruby.util.ByteList key = org.jruby.util.ByteList.create("handlers");

            List handlerList = (List) m.get(key);

            for( Object hm : handlerList) {

                Map me = (Map) hm;
                MapEntry mapEntry = new MapEntry();
                Object o = me.get(org.jruby.util.ByteList.create("script"));

                if (o != null) {
                    mapEntry.script = o.toString();
                    mapEntry.uriPattern = Pattern.compile((me.get(org.jruby.util.ByteList.create("url")).toString()));
                    _handlerList.add(mapEntry);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Currently invoked in _init.js to get the function that is used for
     *  mapUrlToJXP()

     * @return  function to do URI mapping
     * @throws IOException on somethindn bad
     */
    public synchronized JSFunction getFunction() throws IOException {

        /*
         * returns a function that takes scope and path.  Returns either CGIGateway
         * object primed with a script, or null if no match on URI
         */
        return new ed.js.func.JSFunctionCalls0(){

            public Object call(Scope s , Object extra[] ){

                // TODO - examine the yaml Map to see if it specifies a script or
                // if we should just return a null and let the rest of the appserver
                // handle the request - IOW, deal with "static_dir" ...

                for(MapEntry m : _handlerList) {
                    Pattern p = m.uriPattern;

                    Matcher matcher = p.matcher(extra[0].toString());

                    if (matcher.matches()) {
                        return new CGIGateway(m.script, _lib);
                    }
                }
                        
                return null;
            }
        };
    }

    protected String getContent(){
        throw new RuntimeException( "you can't do this" );
    }

    protected InputStream getInputStream(){
        throw new RuntimeException( "you can't do this" );
    }

    public long lastUpdated(Set<Dependency> visitedDeps){
        return _file.lastModified();
    }

    public String getName(){
        return _file.toString();
    }

    public File getFile(){
        return _file;
    }

    class MapEntry {
        Pattern uriPattern;
        String script;
    }
}