//JSFunctionCalls0.java

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

package ed.js.func;

import ed.js.engine.*;
import ed.js.*;
public abstract class JSFunctionCalls0 extends JSFunction { 
    // for Function objects with num args
    public JSFunctionCalls0( int num ){
        super( num );
    }

    public JSFunctionCalls0(){
        super( 0 );
    }

    public JSFunctionCalls0( Scope scope , String name ){
        super( scope , name , 0 );
    }

    public Object call( Scope scope  , Object p0 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 1 ); 
            Object newExtra[] = new Object[1 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 1] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 2 ); 
            Object newExtra[] = new Object[2 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 2] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 3 ); 
            Object newExtra[] = new Object[3 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 3] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 4 ); 
            Object newExtra[] = new Object[4 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 4] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 5 ); 
            Object newExtra[] = new Object[5 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 5] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 6 ); 
            Object newExtra[] = new Object[6 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 6] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 7 ); 
            Object newExtra[] = new Object[7 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 7] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 8 ); 
            Object newExtra[] = new Object[8 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 8] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 9 ); 
            Object newExtra[] = new Object[9 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 9] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 10 ); 
            Object newExtra[] = new Object[10 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 10] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 11 ); 
            Object newExtra[] = new Object[11 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 11] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 12 ); 
            Object newExtra[] = new Object[12 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 12] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 13 ); 
            Object newExtra[] = new Object[13 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 13] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 14 ); 
            Object newExtra[] = new Object[14 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 14] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 15 ); 
            Object newExtra[] = new Object[15 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 15] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 16 ); 
            Object newExtra[] = new Object[16 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 16] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 17 ); 
            Object newExtra[] = new Object[17 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 17] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 18 ); 
            Object newExtra[] = new Object[18 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 18] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 19 ); 
            Object newExtra[] = new Object[19 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 19] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 20 ); 
            Object newExtra[] = new Object[20 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 20] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 21 ); 
            Object newExtra[] = new Object[21 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 21] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 22 ); 
            Object newExtra[] = new Object[22 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 22] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 23 ); 
            Object newExtra[] = new Object[23 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 23] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 24 ); 
            Object newExtra[] = new Object[24 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            newExtra[23] = p23;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 24] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 25 ); 
            Object newExtra[] = new Object[25 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            newExtra[23] = p23;
            newExtra[24] = p24;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 25] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 26 ); 
            Object newExtra[] = new Object[26 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            newExtra[23] = p23;
            newExtra[24] = p24;
            newExtra[25] = p25;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 26] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 27 ); 
            Object newExtra[] = new Object[27 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            newExtra[23] = p23;
            newExtra[24] = p24;
            newExtra[25] = p25;
            newExtra[26] = p26;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 27] = extra[i];
            return call( scope , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object p27 , Object ... extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 28 ); 
            Object newExtra[] = new Object[28 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p0;
            newExtra[1] = p1;
            newExtra[2] = p2;
            newExtra[3] = p3;
            newExtra[4] = p4;
            newExtra[5] = p5;
            newExtra[6] = p6;
            newExtra[7] = p7;
            newExtra[8] = p8;
            newExtra[9] = p9;
            newExtra[10] = p10;
            newExtra[11] = p11;
            newExtra[12] = p12;
            newExtra[13] = p13;
            newExtra[14] = p14;
            newExtra[15] = p15;
            newExtra[16] = p16;
            newExtra[17] = p17;
            newExtra[18] = p18;
            newExtra[19] = p19;
            newExtra[20] = p20;
            newExtra[21] = p21;
            newExtra[22] = p22;
            newExtra[23] = p23;
            newExtra[24] = p24;
            newExtra[25] = p25;
            newExtra[26] = p26;
            newExtra[27] = p27;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 28] = extra[i];
            return call( scope , newExtra );
    }

    protected ThreadLocal<Integer> _lastStart = new ThreadLocal<Integer>();

}
