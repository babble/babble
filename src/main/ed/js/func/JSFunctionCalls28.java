//JSFunctionCalls28.java

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

package ed.js.func;

import ed.js.engine.*;
import ed.js.*;
public abstract class JSFunctionCalls28 extends JSFunction { 
    public JSFunctionCalls28(){
        super( 28 );
    }

    public JSFunctionCalls28( Scope scope , String name ){
        super( scope , name , 28 );
    }

    public Object call( Scope scope  , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 0 ); 
            Object p0 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p1 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p2 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p3 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p4 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p5 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p6 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p7 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p8 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p9 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p10 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p11 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p12 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p13 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p14 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p15 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p16 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p17 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p18 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p19 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p20 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p21 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p22 = extra == null || extra.length <= 22 ? null : extra[22];
            Object p23 = extra == null || extra.length <= 23 ? null : extra[23];
            Object p24 = extra == null || extra.length <= 24 ? null : extra[24];
            Object p25 = extra == null || extra.length <= 25 ? null : extra[25];
            Object p26 = extra == null || extra.length <= 26 ? null : extra[26];
            Object p27 = extra == null || extra.length <= 27 ? null : extra[27];
            Object newExtra[] = extra == null || extra.length <= 28 ? null : new Object[ extra.length - 28];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+28];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 1 ); 
            Object p1 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p2 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p3 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p4 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p5 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p6 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p7 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p8 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p9 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p10 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p11 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p12 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p13 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p14 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p15 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p16 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p17 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p18 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p19 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p20 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p21 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p22 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p23 = extra == null || extra.length <= 22 ? null : extra[22];
            Object p24 = extra == null || extra.length <= 23 ? null : extra[23];
            Object p25 = extra == null || extra.length <= 24 ? null : extra[24];
            Object p26 = extra == null || extra.length <= 25 ? null : extra[25];
            Object p27 = extra == null || extra.length <= 26 ? null : extra[26];
            Object newExtra[] = extra == null || extra.length <= 27 ? null : new Object[ extra.length - 27];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+27];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 2 ); 
            Object p2 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p3 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p4 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p5 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p6 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p7 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p8 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p9 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p10 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p11 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p12 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p13 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p14 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p15 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p16 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p17 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p18 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p19 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p20 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p21 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p22 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p23 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p24 = extra == null || extra.length <= 22 ? null : extra[22];
            Object p25 = extra == null || extra.length <= 23 ? null : extra[23];
            Object p26 = extra == null || extra.length <= 24 ? null : extra[24];
            Object p27 = extra == null || extra.length <= 25 ? null : extra[25];
            Object newExtra[] = extra == null || extra.length <= 26 ? null : new Object[ extra.length - 26];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+26];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 3 ); 
            Object p3 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p4 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p5 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p6 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p7 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p8 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p9 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p10 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p11 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p12 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p13 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p14 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p15 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p16 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p17 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p18 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p19 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p20 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p21 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p22 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p23 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p24 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p25 = extra == null || extra.length <= 22 ? null : extra[22];
            Object p26 = extra == null || extra.length <= 23 ? null : extra[23];
            Object p27 = extra == null || extra.length <= 24 ? null : extra[24];
            Object newExtra[] = extra == null || extra.length <= 25 ? null : new Object[ extra.length - 25];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+25];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 4 ); 
            Object p4 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p5 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p6 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p7 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p8 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p9 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p10 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p11 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p12 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p13 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p14 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p15 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p16 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p17 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p18 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p19 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p20 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p21 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p22 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p23 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p24 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p25 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p26 = extra == null || extra.length <= 22 ? null : extra[22];
            Object p27 = extra == null || extra.length <= 23 ? null : extra[23];
            Object newExtra[] = extra == null || extra.length <= 24 ? null : new Object[ extra.length - 24];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+24];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 5 ); 
            Object p5 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p6 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p7 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p8 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p9 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p10 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p11 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p12 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p13 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p14 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p15 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p16 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p17 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p18 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p19 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p20 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p21 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p22 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p23 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p24 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p25 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p26 = extra == null || extra.length <= 21 ? null : extra[21];
            Object p27 = extra == null || extra.length <= 22 ? null : extra[22];
            Object newExtra[] = extra == null || extra.length <= 23 ? null : new Object[ extra.length - 23];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+23];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 6 ); 
            Object p6 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p7 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p8 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p9 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p10 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p11 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p12 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p13 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p14 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p15 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p16 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p17 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p18 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p19 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p20 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p21 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p22 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p23 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p24 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p25 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p26 = extra == null || extra.length <= 20 ? null : extra[20];
            Object p27 = extra == null || extra.length <= 21 ? null : extra[21];
            Object newExtra[] = extra == null || extra.length <= 22 ? null : new Object[ extra.length - 22];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+22];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 7 ); 
            Object p7 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p8 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p9 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p10 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p11 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p12 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p13 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p14 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p15 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p16 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p17 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p18 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p19 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p20 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p21 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p22 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p23 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p24 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p25 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p26 = extra == null || extra.length <= 19 ? null : extra[19];
            Object p27 = extra == null || extra.length <= 20 ? null : extra[20];
            Object newExtra[] = extra == null || extra.length <= 21 ? null : new Object[ extra.length - 21];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+21];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 8 ); 
            Object p8 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p9 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p10 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p11 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p12 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p13 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p14 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p15 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p16 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p17 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p18 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p19 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p20 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p21 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p22 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p23 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p24 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p25 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p26 = extra == null || extra.length <= 18 ? null : extra[18];
            Object p27 = extra == null || extra.length <= 19 ? null : extra[19];
            Object newExtra[] = extra == null || extra.length <= 20 ? null : new Object[ extra.length - 20];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+20];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 9 ); 
            Object p9 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p10 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p11 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p12 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p13 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p14 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p15 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p16 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p17 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p18 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p19 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p20 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p21 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p22 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p23 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p24 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p25 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p26 = extra == null || extra.length <= 17 ? null : extra[17];
            Object p27 = extra == null || extra.length <= 18 ? null : extra[18];
            Object newExtra[] = extra == null || extra.length <= 19 ? null : new Object[ extra.length - 19];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+19];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 10 ); 
            Object p10 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p11 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p12 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p13 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p14 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p15 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p16 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p17 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p18 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p19 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p20 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p21 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p22 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p23 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p24 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p25 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p26 = extra == null || extra.length <= 16 ? null : extra[16];
            Object p27 = extra == null || extra.length <= 17 ? null : extra[17];
            Object newExtra[] = extra == null || extra.length <= 18 ? null : new Object[ extra.length - 18];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+18];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 11 ); 
            Object p11 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p12 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p13 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p14 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p15 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p16 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p17 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p18 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p19 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p20 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p21 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p22 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p23 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p24 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p25 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p26 = extra == null || extra.length <= 15 ? null : extra[15];
            Object p27 = extra == null || extra.length <= 16 ? null : extra[16];
            Object newExtra[] = extra == null || extra.length <= 17 ? null : new Object[ extra.length - 17];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+17];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 12 ); 
            Object p12 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p13 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p14 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p15 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p16 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p17 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p18 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p19 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p20 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p21 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p22 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p23 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p24 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p25 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p26 = extra == null || extra.length <= 14 ? null : extra[14];
            Object p27 = extra == null || extra.length <= 15 ? null : extra[15];
            Object newExtra[] = extra == null || extra.length <= 16 ? null : new Object[ extra.length - 16];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+16];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 13 ); 
            Object p13 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p14 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p15 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p16 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p17 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p18 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p19 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p20 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p21 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p22 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p23 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p24 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p25 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p26 = extra == null || extra.length <= 13 ? null : extra[13];
            Object p27 = extra == null || extra.length <= 14 ? null : extra[14];
            Object newExtra[] = extra == null || extra.length <= 15 ? null : new Object[ extra.length - 15];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+15];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 14 ); 
            Object p14 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p15 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p16 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p17 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p18 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p19 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p20 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p21 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p22 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p23 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p24 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p25 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p26 = extra == null || extra.length <= 12 ? null : extra[12];
            Object p27 = extra == null || extra.length <= 13 ? null : extra[13];
            Object newExtra[] = extra == null || extra.length <= 14 ? null : new Object[ extra.length - 14];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+14];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 15 ); 
            Object p15 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p16 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p17 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p18 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p19 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p20 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p21 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p22 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p23 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p24 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p25 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p26 = extra == null || extra.length <= 11 ? null : extra[11];
            Object p27 = extra == null || extra.length <= 12 ? null : extra[12];
            Object newExtra[] = extra == null || extra.length <= 13 ? null : new Object[ extra.length - 13];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+13];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 16 ); 
            Object p16 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p17 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p18 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p19 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p20 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p21 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p22 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p23 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p24 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p25 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p26 = extra == null || extra.length <= 10 ? null : extra[10];
            Object p27 = extra == null || extra.length <= 11 ? null : extra[11];
            Object newExtra[] = extra == null || extra.length <= 12 ? null : new Object[ extra.length - 12];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+12];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 17 ); 
            Object p17 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p18 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p19 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p20 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p21 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p22 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p23 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p24 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p25 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p26 = extra == null || extra.length <= 9 ? null : extra[9];
            Object p27 = extra == null || extra.length <= 10 ? null : extra[10];
            Object newExtra[] = extra == null || extra.length <= 11 ? null : new Object[ extra.length - 11];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+11];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 18 ); 
            Object p18 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p19 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p20 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p21 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p22 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p23 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p24 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p25 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p26 = extra == null || extra.length <= 8 ? null : extra[8];
            Object p27 = extra == null || extra.length <= 9 ? null : extra[9];
            Object newExtra[] = extra == null || extra.length <= 10 ? null : new Object[ extra.length - 10];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+10];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 19 ); 
            Object p19 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p20 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p21 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p22 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p23 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p24 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p25 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p26 = extra == null || extra.length <= 7 ? null : extra[7];
            Object p27 = extra == null || extra.length <= 8 ? null : extra[8];
            Object newExtra[] = extra == null || extra.length <= 9 ? null : new Object[ extra.length - 9];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+9];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 20 ); 
            Object p20 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p21 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p22 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p23 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p24 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p25 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p26 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p27 = extra == null || extra.length <= 7 ? null : extra[7];
            Object newExtra[] = extra == null || extra.length <= 8 ? null : new Object[ extra.length - 8];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+8];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 21 ); 
            Object p21 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p22 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p23 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p24 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p25 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p26 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p27 = extra == null || extra.length <= 6 ? null : extra[6];
            Object newExtra[] = extra == null || extra.length <= 7 ? null : new Object[ extra.length - 7];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+7];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 22 ); 
            Object p22 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p23 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p24 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p25 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p26 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p27 = extra == null || extra.length <= 5 ? null : extra[5];
            Object newExtra[] = extra == null || extra.length <= 6 ? null : new Object[ extra.length - 6];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+6];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 23 ); 
            Object p23 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p24 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p25 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p26 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p27 = extra == null || extra.length <= 4 ? null : extra[4];
            Object newExtra[] = extra == null || extra.length <= 5 ? null : new Object[ extra.length - 5];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+5];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 24 ); 
            Object p24 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p25 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p26 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p27 = extra == null || extra.length <= 3 ? null : extra[3];
            Object newExtra[] = extra == null || extra.length <= 4 ? null : new Object[ extra.length - 4];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+4];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 25 ); 
            Object p25 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p26 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p27 = extra == null || extra.length <= 2 ? null : extra[2];
            Object newExtra[] = extra == null || extra.length <= 3 ? null : new Object[ extra.length - 3];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+3];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 26 ); 
            Object p26 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p27 = extra == null || extra.length <= 1 ? null : extra[1];
            Object newExtra[] = extra == null || extra.length <= 2 ? null : new Object[ extra.length - 2];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+2];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 27 ); 
            Object p27 = extra == null || extra.length <= 0 ? null : extra[0];
            Object newExtra[] = extra == null || extra.length <= 1 ? null : new Object[ extra.length - 1];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+1];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , p14 , p15 , p16 , p17 , p18 , p19 , p20 , p21 , p22 , p23 , p24 , p25 , p26 , p27 , newExtra );
    }

    protected ThreadLocal<Integer> _lastStart = new ThreadLocal<Integer>();

}
