// TemplateEngine.java

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

package ed.appserver.templates;

import java.io.*;
import java.util.*;

import ed.util.*;
import ed.lang.*;

public class TemplateEngine {
    
    static final File tmp = new File( "/tmp/jxp/templates/" );
    static final boolean OUTPUT = true;

    public static TemplateConverter.Result oneConvert( Template t , DependencyTracker tracker ){
        TemplateConverter tc = _builtIns.get( t.getExtension() );
        if ( tc == null )
            return null;
        
        try {
            final TemplateConverter.Result r = tc.convert( t , tracker );
        
            if ( OUTPUT ){
                try {
                    File f = new File( tmp , r.getNewTemplate().getName() );
                    f.getParentFile().mkdirs();
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

        _builtIns.put( "rb" , new ed.lang.ruby.RubyConvert.TemplateImpl() );
        _builtIns.put( "erb" , new RubyTemplateConverter( "erb" ) );
        _builtIns.put( "rhtml" , new RubyTemplateConverter( "rhtml" ) );
        _builtIns.put( "txt" , new RubyTemplateConverter("txt"));
    } 
}
