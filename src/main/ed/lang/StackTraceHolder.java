// StackTraceHolder.java

package ed.lang;

import java.util.*;

public class StackTraceHolder {

    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );
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

    public void set( String fullName , StackTraceFixer fixer ){
        if ( DEBUG ) System.out.println( "set [" + fullName + "]" );
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
        
        if ( l.size() == 0 )
            return null;

        return l;
    }
    
    /**
     * @return null if should be removed, or the correct thing
    */
    public StackTraceElement fix( StackTraceElement element ){
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
		
		element = n;
		continue fixerLoop;
	    }    
            
	    break;
	}
	return element;
    }
    
    public void fix( Throwable t ){
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
}
