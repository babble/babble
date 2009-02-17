// TemplateEngine.java

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

import java.io.*;
import java.util.*;

import ed.util.*;
import ed.lang.*;

public class TemplateEngine {
    
    static final boolean OUTPUT = true;
    
    public static TemplateConverter.Result oneConvert( Template t , DependencyTracker tracker ){
        TemplateConverter tc = _builtIns.get( t.getExtension() );
        if ( tc == null )
            return null;
        
        try {
            final TemplateConverter.Result r = tc.convert( t , tracker );
        
            if ( OUTPUT ){
                try {
                    File f = ed.io.WorkingFiles.getTMPFile( "templates" , r.getNewTemplate().getName() );
                    FileOutputStream fout = new FileOutputStream( f );
                    fout.write( r.getNewTemplate().getContent().getBytes() );
                    fout.close();
                }
                catch ( IOException ioe ){
                    ioe.printStackTrace();
                }
            }

            if ( r.getLineMapping() != null ){
                final StackTraceHolder sth = StackTraceHolder.getInstance();
                sth.set( r.getNewTemplate().getName() , 
                         new BasicLineNumberMapper( t.getName() , r.getNewTemplate().getName() , r.getLineMapping() ) );
            }
                
            return r;
        }
        catch ( RuntimeException re ){
            if ( re.toString().contains( t.getName() ) )
                throw re;
            throw new RuntimeException( t.getClass() + " failed to convert [" + t.getName() + "] b/c of " + re , re );
        }
    }
    
    private static final Map<String,TemplateConverter> _builtIns = new TreeMap<String,TemplateConverter>();
    static {
        _builtIns.put( "jxp" , new JxpConverter() );
        _builtIns.put( "html" , new JxpConverter( true ) );

        _builtIns.put( "txt" , new RubyTemplateConverter("txt"));
    } 
}
