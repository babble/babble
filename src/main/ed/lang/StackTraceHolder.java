// StackTraceHolder.java

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

package ed.lang;

import java.util.*;

/**
 * Class to clean up a stack trace for presentation to user.
 *
 * Comprises a set of StackTraceFixers which are applied to Throwables or
 * individual StackTraceElements. The idea is to remove "internal" stack frames
 * that the user isn't likely to care about, and replace filenames and class
 * names with information that actually applies to user code.
 */
public class StackTraceHolder {

    public static interface NoFix{}

    public static final boolean RAW_EXCPETIONS = ed.util.Config.get().getBoolean( "RAWE" );
    public static final boolean DEBUG = Boolean.getBoolean( "DEBUG.ST" );

    private static final StackTraceHolder INSTANCE = new StackTraceHolder();
    public static final StackTraceHolder getInstance(){
        return INSTANCE;
    }

    private StackTraceHolder(){

    }
    
    public void setPackage( String pack , StackTraceFixer fixer ){
        _packs.put( pack , fixer );
    }

    public void setFileType( String ext , StackTraceFixer fixer ){
        _fileexts.put( ext , fixer );
    }

    public void set( String fullName , StackTraceFixer fixer ){
        if ( DEBUG ) System.out.println( "set [" + fullName + "] " + fixer );
        _fixers.put( fullName , fixer );
    }
    
    List<StackTraceFixer> getRelevant( StackTraceElement element ){
        List<StackTraceFixer> l = new LinkedList<StackTraceFixer>(){
            public boolean add( StackTraceFixer f ){
                if ( f == null )
                    return false;
                return super.add( f );
            }
        };

        final String cn = element.getClassName();
        if ( DEBUG ) System.out.println( "get [" + cn + "]" );

        l.add( _fixers.get( cn ) );
        {
            final int idx = cn.indexOf( "$" );
            if ( idx > 0 ){
                final String s = cn.substring( 0 , idx );
                l.add( _fixers.get( s ) );
            }
        }

        {
            final int idx = cn.lastIndexOf( "." );
            if ( idx > 0 ){
                String p = cn.substring( 0 , idx );
                l.add( _packs.get( p ) );
            }
        }

        final String fn = element.getFileName();
        {
            final int idx = fn.lastIndexOf( "." );
            if( idx > 0 ){
                String ext = fn.substring( idx+1 );
                l.add( _fileexts.get( ext ) );
            }
        }

        if ( l.size() == 0 )
            return null;

        return l;
    }
    
    /**
     * Fixes a given stack trace element.
     *
     * Returns null if this stack trace should be removed -- ie it's internal.
     * Return a new stack trace element if this element should be replaced
     * with a different/more informative element.
     *
     * @return null if should be removed, or the correct thing
    */
    public StackTraceElement fix( StackTraceElement element ){
        
        if ( RAW_EXCPETIONS )
            return element;
        
	if ( element == null )
	    return null;
        
	fixerLoop:
	while ( true ){

            List<StackTraceFixer> fixers = getRelevant( element );
            if ( fixers == null )
                return element;
    
	    for ( StackTraceFixer f : fixers )
		if ( f.removeSTElement( element ) )
		    return null;
	    
	    for ( StackTraceFixer f : fixers ){
		StackTraceElement n = f.fixSTElement( element );
		if ( n == null || n == element )
		    continue;
		
                if ( DEBUG ) System.out.println( element + " -->> " + n );

		element = n;
		continue fixerLoop;
	    }    
            
	    break;
	}
	return element;
    }
    
    public void fix( Throwable t ){
        fix(t, true);
    }
    public void fix(Throwable t, boolean recurse) {

        if ( t instanceof NoFix )
            return;
        
        while(t != null) {
            _fix(t);
            
            if(!recurse)
                break;
            
            t = t.getCause();
        }
    }
    private void _fix( Throwable t ){
        if ( t == null )
            return;
        
        if ( RAW_EXCPETIONS )
            return;
        
        boolean removeThings = false;
        boolean changed = false;
        
        StackTraceElement stack[] = t.getStackTrace();

        for ( int i=0; i<stack.length; i++ ){
            
            final StackTraceElement element = stack[i];
            if ( element == null )
                continue;
	    
	    final StackTraceElement n = fix( element );
	    if ( n == element )
		continue;

	    stack[i] = n;
	    changed = true;
	    
	    if ( n == null )
		removeThings = true;
        }
        
        if ( removeThings ){
            changed = true;
            
            List<StackTraceElement> lst = new ArrayList<StackTraceElement>();
            for ( StackTraceElement s : stack ){
                if ( s == null )
                    continue;
                lst.add( s );
            }
            stack = new StackTraceElement[lst.size()];
            for ( int i=0; i<stack.length; i++ )
                stack[i] = lst.get(i);
        }
            
        if ( changed )
            t.setStackTrace( stack );
    }
 
    final Map<String,StackTraceFixer> _fixers = Collections.synchronizedMap( new HashMap<String,StackTraceFixer>() );
    final Map<String,StackTraceFixer> _packs = Collections.synchronizedMap( new TreeMap<String,StackTraceFixer>() );
    final Map<String,StackTraceFixer> _fileexts = Collections.synchronizedMap( new HashMap<String,StackTraceFixer>() );
}
