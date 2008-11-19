// Simulator.java

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

package ed.net.lb;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import ed.TestCase;

public class SimulatorTest extends TestCase { 

    public void setupStuff( File f ) 
        throws Exception {
        Simulator.clearDB();
        Simulator.simulate( new FileReader( f ) );
    }

    @Test
    public void testBasic1() 
        throws Exception {
        setupStuff( new File( "./src/test/ed/net/lb/basic1.txt" ) );
    }

    @Test
    public void testBasic2() 
        throws Exception {
        setupStuff( new File( "./src/test/ed/net/lb/basic2a.txt" ) );

        for( int i=14521; i<14619; i++) {
            Simulator.killAddress( i, 10000 );
        }
        Simulator.simulate( new FileReader( new File( "./src/test/ed/net/lb/basic2b.txt" ) ) );
    }

    public static void main( String[] args ) 
        throws Exception {

        SimulatorTest st = new SimulatorTest();

        st.testBasic1();
        st.testBasic2();
    }
}