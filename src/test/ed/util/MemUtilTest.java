// MemUtilTest.java

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

package ed.util;

import java.io.*;
import java.util.*;

import org.testng.annotations.Test;

import ed.util.*;
import static ed.util.MemUtil.*;

public class MemUtilTest extends ed.TestCase {

    @Test
    public void testJavaIsGC(){
        assertTrue( GCLine.isGCLine( "2369.623: [GC [PSYoungGen: 253491K->57715K(270720K)] 662375K->492170K(760896K), 0.1937260 secs] [Times: user=0.24 sys=0.00, real=0.20 secs] " ) );
        assertTrue( GCLine.isGCLine( "2378.656: [GC [PSYoungGen: 259315K->53474K(271616K)] 693770K->491671K(761792K), 0.1463140 secs] [Times: user=0.16 sys=0.00, real=0.14 secs] " ) );
        assertTrue( GCLine.isGCLine( "2388.453: [GC [PSYoungGen: 257442K->56577K(271616K)] 695639K->498343K(761792K), 0.1480230 secs] [Times: user=0.17 sys=0.00, real=0.14 secs] " ) );
        assertTrue( GCLine.isGCLine( "2394.839: [GC [PSYoungGen: 260545K->52847K(272960K)] 702311K->509225K(763136K), 0.1738920 secs] [Times: user=0.19 sys=0.00, real=0.18 secs] " ) );
        assertTrue( GCLine.isGCLine( "2395.013: [Full GC [PSYoungGen: 52847K->0K(272960K)] [PSOldGen: 456378K->287193K(508160K)] 509225K->287193K(781120K) [PSPermGen: 44209K->44209K(44992K)], 0.8087710 secs] [Times: user=0.81 sys=0.00, real=0.80 secs] " ) );

        assertTrue( GCLine.isGCLine( "[GC [PSYoungGen: 260545K->52847K(272960K)] 702311K->509225K(763136K), 0.1738920 secs] [Times: user=0.19 sys=0.00, real=0.18 secs] " ) );
        assertTrue( GCLine.isGCLine( "[Full GC [PSYoungGen: 52847K->0K(272960K)] [PSOldGen: 456378K->287193K(508160K)] 509225K->287193K(781120K) [PSPermGen: 44209K->44209K(44992K)], 0.8087710 secs] [Times: user=0.81 sys=0.00, real=0.80 secs] " ) );

        assertTrue( GCLine.isGCLine( "46954.065: [Full GC [PSYoungGen: 100608K->100594K(225984K)] [PSOldGen: 819199K->819199K(819200K)] 919807K->919794K(1045184K) [PSPermGen: 46936K->46936K(47232K)], 1.8832360 secs]" ) );


        // 1.5
        assertTrue( GCLine.isGCLine( "105.350: [GC [PSYoungGen: 106006K->25000K(145024K)] 228802K->167810K(1874496K), 0.0308960 secs]" ) );
        
        assertFalse( GCLine.isGCLine( "Asdasd" ) );

        assertFalse( GCLine.isGCLine( "Exception in thread \"ThreadPool.MyThread:HttpServer-main:2\" java.lang.OutOfMemoryError: GC overhead limit exceeded\"" ) );
        assertFalse( GCLine.isGCLine( "Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded" ) );
        assertFalse( GCLine.isGCLine( "java.lang.OutOfMemoryError: GC overhead limit exceeded" ) );
        assertFalse( GCLine.isGCLine( "java.lang.OutOfMemoryError: GC overhead limit exceeded" ) );
        assertFalse( GCLine.isGCLine( "\"GC task thread#0 (ParallelGC)\" prio=10 tid=0x000000004011d800 nid=0x58c7 runnable " ) );
        assertFalse( GCLine.isGCLine( "\"GC task thread#1 (ParallelGC)\" prio=10 tid=0x000000004011ec00 nid=0x58c8 runnable " ) );
        assertFalse( GCLine.isGCLine( "[11/02/2008 01:53:30.475 EST] ThreadPool.MyThread:HttpServer-main:15 || clusterstock:www.slow INFO >> /Gannett-Co-GCI-stock-financial-analysis.html 2615ms" ) );
        assertFalse( GCLine.isGCLine( "Exception in thread \"ThreadPool.MyThread:HttpServer-main:29\" java.lang.OutOfMemoryError: GC overhead limit exceeded" ) );
        assertFalse( GCLine.isGCLine( "java.lang.OutOfMemoryError: GC overhead limit exceeded" ) );

    }

    @Test
    public void testParse1(){
        _testParse( "2394.839: [GC [PSYoungGen: 260545K->52847K(272960K)] 702311K->509225K(763136K), 0.1738920 secs] [Times: user=0.19 sys=0.00, real=0.18 secs] " ,
                    2394839 , false , 180 );

        _testParse( "[Full GC [PSYoungGen: 52847K->0K(272960K)] [PSOldGen: 456378K->287193K(508160K)] 509225K->287193K(781120K) [PSPermGen: 44209K->44209K(44992K)], 0.8087710 secs] [Times: user=0.81 sys=0.00, real=0.80 secs] " ,
                    -1 , true , 800 );

        _testParse( "7.12: [Full GC [PSYoungGen: 52847K->0K(272960K)] [PSOldGen: 456378K->287193K(508160K)] 509225K->287193K(781120K) [PSPermGen: 44209K->44209K(44992K)], 0.8087710 secs] [Times: user=0.81 sys=0.00, real=0.80 secs] " ,
                    7120 , true , 800 );

        _testParse( "7.012: [Full GC [PSYoungGen: 52847K->0K(272960K)] [PSOldGen: 456378K->287193K(508160K)] 509225K->287193K(781120K) [PSPermGen: 44209K->44209K(44992K)], 0.8087710 secs] [Times: user=0.81 sys=0.00, real=0.80 secs] " ,
                    7012 , true , 800 );
    }

    @Test
    public void testParse2(){
        _testParse( "[GC [PSYoungGen: 260545K->52847K(272960K)] 702311K->509225K(763136K), 0.1738920 secs] [Times: user=0.19 sys=0.00, real=0.18 secs] " ,
                    -1 , false , 180 );
    
    }

    @Test
    public void test15parse(){
        _testParse( "105.350: [GC [PSYoungGen: 106006K->25000K(145024K)] 228802K->167810K(1874496K), 0.0308960 secs]" ,
                    105350 , false , 30);

        _testParse( "405.910: [Full GC [PSYoungGen: 34126K->0K(145920K)] [PSOldGen: 1740897K->1359078K(1751296K)] 1775023K->1359078K(1897216K) [PSPermGen: 22334K->22173K(45568K)], 3.4769290 secs]" ,
                    405910 , true , 3476 );

    }
    
    @Test
    public void testparse3(){
        _testParse( "120.944: [Full GC [PSYoungGen: 482K->0K(80768K)] [PSOldGen: 199383K->184138K(204800K)] 199865K->184138K(285568K) [PSPermGen: 39425K->39425K(47168K)], 0.6940380 secs]" ,
                    120944 , true , 694 );
    }

    void _testParse( String line , long when , boolean full , long howLong ){
        GCLine l = GCLine.parse( line );
        assert( l != null );

        assertEquals( when , l.when() );
        assertEquals( full , l.full() );
        assertEquals( howLong , l.howLong() );
    }

    @Test
    public void testGCStream1(){
        GCStream s = new GCStream();
        assertEquals( 0.0 , s.fullGCPercentage() );
        
        s.add( new GCLine( 0000 , false , 5 ) );
        s.add( new GCLine( 1000 , false , 5 ) );
        s.add( new GCLine( 2000 , false , 5 ) );
        s.add( new GCLine( 3000 , false , 5 ) );
        s.add( new GCLine( 4000 , false , 5 ) );
        s.add( new GCLine( 5000 , false , 5 ) );

        assertEquals( 0.0 , s.fullGCPercentage() );
    }

    @Test
    public void testGCStream2(){
        GCStream s = new GCStream();
        assertEquals( 0.0 , s.fullGCPercentage() );
        
        s.add( new GCLine( 0000 , true , 500 ) );
        s.add( new GCLine( 1000 , true , 500 ) );
        s.add( new GCLine( 2000 , true , 500 ) );
        s.add( new GCLine( 3000 , true , 500 ) );
        s.add( new GCLine( 4000 , true , 500 ) );
        assertEquals( 0.0 , s.fullGCPercentage() , 0 );
        s.add( new GCLine( 5000 , true , 500 ) );

        assertEquals( .6 , s.fullGCPercentage() , 0 );
    }

    @Test
    public void testGCStream3(){
        GCStream s = new GCStream();
        assertEquals( 0.0 , s.fullGCPercentage() );
        
        s.add( new GCLine( 0000 , true , 500 ) );
        s.add( new GCLine( 1000 , false , 500 ) );
        s.add( new GCLine( 2000 , true , 500 ) );
        s.add( new GCLine( 3000 , false , 500 ) );
        s.add( new GCLine( 4000 , true , 500 ) );
        assertEquals( 0.0 , s.fullGCPercentage() , 0 );
        s.add( new GCLine( 5000 , true , 500 ) );

        assertEquals( .4 , s.fullGCPercentage() , 0 );
    }

    public static void main( String args[] ){
        (new MemUtilTest()).runConsole();
    }
}

