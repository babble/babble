// LBApp.java

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

public class LBApp extends XGenJavaApp {

    LBApp( String name , OptionMap options ){
        super( "lb" , name , "ed.net.lb.LB" , options , _getArgs( options ) , 300 );
    }
    
    static List<String> _getArgs( OptionMap options ){
        List<String> l = new ArrayList<String>();

        if ( options.getInt( "port" , 0 ) > 0 ){
            l.add( "--port" );
            l.add( options.get( "port" ) );
        }
        
        return l;
    }
}
