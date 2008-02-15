// JxpSourceTest.java

package ed.appserver.jxp;

import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpSourceTest extends ed.TestCase {
    
    static String LOOK = "Lhasd08y1lknsxuhdoahsd";

    static class StringSource extends JxpSource {
        StringSource( String s ){
            _s = s;
        }
        
        String getName(){
            return "temp.jxp";
        }

        String getContent() {
            return _s;
        }
        
        long lastUpdated(){
            return _t;
        }

        final String _s;
        final long _t = System.currentTimeMillis();
    }


    public void test0(){
        _test( "<%= " + LOOK + " %>\n" );
    }
    public void test1(){
        _test( "<%       print( { c : " + LOOK + "  }  ); %>" );
    }

    public void test2(){
        _test( "<%       print( { c : " + LOOK + "  }  ); %>\n" );
    }

    public void test3(){
        _test( "<div>\nasdasd\n<%= asd %>\n<%       print( { c : " + LOOK + "  }  ); %>\nasdas" );
    }

    
    void _test( String s ){

        boolean debug = false;
        
        if ( debug ) System.out.println( "-----" );
        try {
            StringSource ss = new StringSource( s );
            ss.getFunction();
            if ( debug ){
                System.out.println( ss._jsCode );
                System.out.println( "-" );
            }
            
            int sourceLine = _find( s );
            int jsLine = _find( ss._jsCode );
            if ( debug ){
                System.out.println( "sourceLine : " + sourceLine );
                System.out.println( "jsLine : " + jsLine );
            }

            assertEquals( sourceLine , ss.getSourceLine( jsLine ) );
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
        if ( debug ) System.out.println( "-----" );
    }
    
    static int _find( String s ){
        int idx = s.indexOf( LOOK );
        if ( idx < 0 )
            throw new RuntimeException( "need to put the LOOK think in your case"  );
        
        int num = 1;
        for ( int i=0; i<idx; i++ )
            if ( s.charAt( i ) == '\n' )
                num++;
        
        return num;
    }

    public static void main( String args[] ){
        (new JxpSourceTest()).runConsole();
    }
}
