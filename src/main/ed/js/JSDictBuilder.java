// JSDictBuilder.java

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

package ed.js;

public class JSDictBuilder {

    public static JSDictBuilder start(){
        return new JSDictBuilder();
    }

    public JSDictBuilder(){
        _theDict = new JSDict();
    }
    
    public JSDictBuilder set( String key , Object value ){
        _theDict.put( key , value );
        return this;
    }

    public JSDictBuilder put( String key , Object value ){
        _theDict.put( key , value );
        return this;
    }

    public JSDict get(){
        return _theDict;
    }
    
    final JSDict _theDict;
}
