// HeadArray.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
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
