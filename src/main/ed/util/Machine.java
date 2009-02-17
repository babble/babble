// Machine.java

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
