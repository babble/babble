// Machine.java

package ed.util;

public class Machine {
    
    public static enum OSType { 
        MAC , LINUX , WIN , OTHER;

        public boolean isMac(){
            return this == MAC;
        }

        public boolean isLinux(){
            return this == LINUX;
        }

    };

    static final OSType _os;
    static {
        OSType me = null;
        String osName = System.getProperty( "os.name" ).toLowerCase();
        if ( osName.indexOf( "linux" ) >= 0 )
            me = OSType.LINUX;
        else if ( osName.indexOf( "mac" ) >= 0 )
            me = OSType.MAC;
        else if ( osName.indexOf( "win" ) >= 0 )
            me = OSType.WIN;
        else {
            System.err.println( "unknown os name [" + osName + "]" );
            me = OSType.OTHER;
        }
        _os = me;
    }
    
    public static OSType getOSType(){
        return _os;
    }
    
}
