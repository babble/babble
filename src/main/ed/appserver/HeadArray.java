// HeadArray.java

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
