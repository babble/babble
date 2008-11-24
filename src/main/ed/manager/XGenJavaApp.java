// XGenJavaApp.java

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
