// HeadArray.java

package ed.appserver;

import ed.js.*;

public class HeadArray extends JSArray {

    public void addCSS( String css ){
        add( "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + css + "\" >" );
    }

    public void addScript( String script ){
        add( "<script src=\"" + script + "\" ></script>" );
    }

    public void addRSS( String link ){
	add("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"" + link + "\" />" );
    }
}
