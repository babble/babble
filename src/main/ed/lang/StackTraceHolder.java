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
    
    public void fix( Throwable t ){
        if ( RAW_EXCPETIONS )
            return;
        
        boolean removeThings = false;
        boolean changed = false;
        
        StackTraceElement stack[] = t.getStackTrace();

        for ( int i=0; i<stack.length; i++ ){
            
            StackTraceElement element = stack[i];
            if ( element == null )
                continue;
            
            List<StackTraceFixer> fixers = getRelevant( element );
            if ( fixers == null )
                continue;

            fixerLoop:
            while ( true ){
                
                for ( StackTraceFixer f : fixers ){
                    if ( ! f.removeSTElement( element ) )
                        continue;
                    
                    removeThings = true;
                    stack[i] = null;
                    break fixerLoop;
                }
                
                for ( StackTraceFixer f : fixers ){
                    StackTraceElement n = f.fixSTElement( element );
                    if ( n == null || n == element )
                        continue;
                    
                    stack[i] = n;
                    element = n;
                    changed = true;
                    continue fixerLoop;
                }    
                
                break;
            }
            
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
