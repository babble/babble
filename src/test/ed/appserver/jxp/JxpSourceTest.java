// JxpSourceTest.java

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

package ed.appserver.jxp;

import java.io.*;
import java.util.Set;

import org.testng.annotations.Test;

import ed.util.Dependency;

public class JxpSourceTest extends ed.TestCase {
    
    static class StringSource extends JxpSource {
        StringSource( String s ){
            _s = s;
        }

        public File getFile(){
            return null;
        }
        
        public String getName(){
            return "temp.jxp";
        }

        protected String getContent() {
            return _s;
        }

        protected InputStream getInputStream(){
            return new ByteArrayInputStream( _s.getBytes() );
        }
        
        public long lastUpdated(Set<Dependency> visitedDeps){
            visitedDeps.add(this);
            return _t;
        }

        final String _s;
        final long _t = System.currentTimeMillis();
    }


    public static void main( String args[] ){
        (new JxpSourceTest()).runConsole();
    }
}
