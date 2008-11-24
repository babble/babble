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
    
    @Test
    public void testGCStream4(){
        GCStream s = new GCStream();

        assertEquals( 0.0 , s.fullGCPercentage() );
    
        long time = 1000;
        
        for ( int i=0; i<100; i++ ){
            s.add( new GCLine( time , false , 100 ) );
            time += 1000;
        }
        
        assertEquals( 0.0 , s.fullGCPercentage() );
        
        s.add( new GCLine( time , true , 2000 ) );
        assertEquals( 0.105 , s.fullGCPercentage() , .01 );
    }

    @Test
    public void testGCStream5(){
        GCStream s = new GCStream();
        
        assertTrue( s.add( "1313.919: [GC [PSYoungGen: 324803K->51506K(343744K)] 864899K->594837K(926976K), 0.1065260 secs]" ) );
        assertTrue( s.add( "1319.303: [GC [PSYoungGen: 330790K->35957K(344448K)] 874121K->579345K(927680K), 0.0739770 secs]" ) );
        assertTrue( s.add( "1321.351: [GC [PSYoungGen: 315246K->34913K(343232K)] 858633K->578404K(926464K), 0.0632070 secs]" ) );
        assertTrue( s.add( "1325.257: [GC [PSYoungGen: 314721K->27079K(343936K)] 858212K->570594K(927168K), 0.0537650 secs]" ) );
        assertTrue( s.add( "1327.040: [GC [PSYoungGen: 306887K->31759K(344704K)] 850402K->575290K(927936K), 0.0658280 secs]" ) );
        assertTrue( s.add( "1332.575: [GC [PSYoungGen: 312463K->42887K(323648K)] 855994K->586450K(906880K), 0.0807930 secs]" ) );
        assertTrue( s.add( "1335.874: [GC [PSYoungGen: 323591K->32320K(344896K)] 867154K->580044K(928128K), 0.0682390 secs]" ) );
        assertTrue( s.add( "1342.048: [GC [PSYoungGen: 314624K->31640K(344128K)] 862348K->579711K(927360K), 0.0645430 secs]" ) );
        assertTrue( s.add( "1342.462: [GC [PSYoungGen: 313944K->43344K(346880K)] 862015K->591536K(930112K), 0.0662180 secs]" ) );
        assertTrue( s.add( "1342.938: [GC [PSYoungGen: 329168K->59715K(345600K)] 877360K->607949K(928832K), 0.0968700 secs]" ) );
        assertTrue( s.add( "1343.479: [GC [PSYoungGen: 345539K->63991K(313344K)] 893773K->634916K(896576K), 0.1513180 secs]" ) );
        assertTrue( s.add( "1344.076: [GC [PSYoungGen: 313335K->80121K(329472K)] 884260K->660739K(912704K), 0.1366180 secs]" ) );

        assertTrue( s.add( "1344.213: [Full GC [PSYoungGen: 80121K->6958K(329472K)] [PSOldGen: 580618K->583232K(672064K)] 660739K->590190K(1001536K) [PSPermGen: 56203K->56203K(59392K)], 1.9110120 secs]" ) );
        assertEquals( 0.076 , s.fullGCPercentage() , .01 );

        assertTrue( s.add( "1354.213: [Full GC [PSYoungGen: 80121K->6958K(329472K)] [PSOldGen: 580618K->583232K(672064K)] 660739K->590190K(1001536K) [PSPermGen: 56203K->56203K(59392K)], 6.9110120 secs]" ) );
        assertEquals( 0.407 , s.fullGCPercentage() , .01 );

        assertTrue( s.add( "1364.213: [Full GC [PSYoungGen: 80121K->6958K(329472K)] [PSOldGen: 580618K->583232K(672064K)] 660739K->590190K(1001536K) [PSPermGen: 56203K->56203K(59392K)], 6.9110120 secs]" ) );
        assertEquals( 0.709 , s.fullGCPercentage() , .01 );

        assertTrue( s.add( "1374.213: [Full GC [PSYoungGen: 80121K->6958K(329472K)] [PSOldGen: 580618K->583232K(672064K)] 660739K->590190K(1001536K) [PSPermGen: 56203K->56203K(59392K)], 6.9110120 secs]" ) );
        assertEquals( 1.036 , s.fullGCPercentage() , .01 );
    }
    
    public static void main( String args[] ){
        (new MemUtilTest()).runConsole();
    }
}

