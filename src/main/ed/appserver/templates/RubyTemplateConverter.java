// RubyTemplateConverter.java

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

package ed.appserver.templates;

import java.util.*;

import ed.util.*;
import ed.js.engine.*;
import ed.lang.ruby.*;

public class RubyTemplateConverter extends HtmlLikeConverter {

    public RubyTemplateConverter( String ext ){
        super( ext , _codeTags , ed.lang.Language.RUBY() );
    }

    protected String getNewName( Template t ){
        return t.getName().replaceAll( "\\.(\\w+)+$" , "_$1.rb" );
    }

    protected void gotCode( Generator g , CodeMarker cm , String code ){

        if ( code.startsWith( "-" ) )
            code = code.substring( 1 );
        if ( code.endsWith( "-" ) )
            code = code.substring( 0 , code.length() - 1 );

        if ( cm._startTag.equals( "<%=" ) ){
            g.append( "print( " );
            g.append( code );
            g.append( " );\n" );
            return;
        }

        if ( cm._startTag.equals( "<%" ) ){
            g.append( code );
            g.append( "\n" );
            return;
        }
    }

    protected boolean gotStartTag( Generator gg , String tag , State state ){
        return false;
    }
    
    protected boolean gotEndTag( Generator gg , String tag , State state ){
        return false;
    }
    
    static List<CodeMarker> _codeTags = new ArrayList<CodeMarker>();
    static {
        _codeTags.add( new CodeMarker( "<%=" , "%>" ) );
        _codeTags.add( new CodeMarker( "<%" , "%>" ) );
    }

}
