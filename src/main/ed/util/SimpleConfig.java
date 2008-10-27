// SimpleConfig.java

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

package ed.util;

import java.util.*;

public interface SimpleConfig {

    public void addEntry( String type , String name , String key , String value );
    public void addValue( String type , String name , String value );

    public Map<String,String> getMap( String type , String name );
    public List<String> getValues( String type , String name );

    public List<String> getTypes();
    public List<String> getNames( String type );
    
    public boolean isMap( String type , String name );
    public boolean isValue( String type , String name );
}
