// HeadArray.java

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

package ed.appserver;

import ed.js.*;

public class HeadArray extends JSArray {

    protected int getIndex( Object index ){
        int idx;
        if( index == null ) idx = size();
        else {
            if( ! (index instanceof Number) ) throw new JSException( "index must be number, not " + index.toString() );
            idx = ((Number)index).intValue();
        }
        return idx;
    }

    public void addCSS( String css , Object index ){
        add( getIndex( index ) , 
             "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + css + "\" >" );
    }

    public void addScript( String script , Object index ){
        add( getIndex( index ) ,
             "<script src=\"" + script + "\" ></script>" );
    }

    public void addRSS( String link ){
	add("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"" + link + "\" />" );
    }
}
