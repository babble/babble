// XGenJavaApp.java

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

package ed.manager;

import java.io.*;
import java.util.*;

import ed.util.*;

public abstract class XGenJavaApp extends JavaApplication {

    XGenJavaApp( String type , String name , String className , OptionMap options , List<String> args , int memory ){
        super( type , name , className , memory , args , _getJvmArgs( options ) , true );
    }
    
    static List<String> _getJvmArgs( OptionMap options ){
        List<String> l = new ArrayList<String>();

        l.add( "-enableassertions" );
        l.add( "-Djava.library.path=include" );
        l.add( "-Djava.awt.headless=true" );
        l.add( "-Djruby.home=./include/ruby" );
        l.add( "-XX:MaxDirectMemorySize=600M" );


        return l;
    }

}
