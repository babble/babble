// StackTraceHolder.java

package ed.lang;

import java.util.*;

public class StackTraceHolder {

    public static final boolean RAW_EXCPETIONS = Boolean.getBoolean( "RAWE" );
    
    private static final StackTraceHolder INSTANCE = new StackTraceHolder();
    public static final StackTraceHolder getInstance(){
        return INSTANCE;
    }

    private StackTraceHolder(){

    }

    public void set( String fullName , StackTraceFixer fixer ){
        _fixers.put( fullName , fixer );
    }
    
    List<StackTraceFixer> getRelevant( StackTraceElement element ){
        StackTraceFixer fixer = _fixers.get( element.getClassName() );
        if ( fixer == null )
            return null;
        List<StackTraceFixer> l = new LinkedList<StackTraceFixer>();
        l.add( fixer );
        return l;
    }

    /**
     * @return null if should be removed, or the correct thing
    */
    public StackTraceElement fix( StackTraceElement element ){
	if ( element == null )
	    return null;
	
	List<StackTraceFixer> fixers = getRelevant( element );
	if ( fixers == null )
	    return element;

	fixerLoop:
	while ( true ){
	    
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
 
    final Map<String,StackTraceFixer> _fixers = new HashMap<String,StackTraceFixer>();
   
}
