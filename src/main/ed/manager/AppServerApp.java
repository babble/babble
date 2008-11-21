// AppServerApp.java

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

public class AppServerApp extends XGenJavaApp {

    AppServerApp( String name , OptionMap options ){
        super( "appserver" , name , "ed.appserver.AppServer" , options , new LinkedList<String>() );
        //super( "appserver" , name , "ed.appserver.AppServer" , _howMuchMemory( options ) , _getArgs( options ) , _getJvmArgs( options ) , true );
    }

}
