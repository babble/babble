// JxpConvertTest.java

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

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.*;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class JxpConvertTest extends ConvertTestBase {

    public JxpConvertTest(){
        this( null );
    }

    public JxpConvertTest( String args[] ){
        super( ".jxp" , args );
    }


    @Factory 
    public Object[] getAllTests(){
        return _all.toArray();
    }

    TemplateConverter getConverter(){
        return new JxpConverter();
    }

    public static void main( String args[] ){
        JxpConvertTest t = new JxpConvertTest( args );
        t.runConsole();
    }
    
}
