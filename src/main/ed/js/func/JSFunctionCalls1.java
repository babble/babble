//JSFunctionCalls1.java

package ed.js.func;

import ed.js.engine.*;
import ed.js.*;
public abstract class JSFunctionCalls1 extends JSFunction { 
    public JSFunctionCalls1(){
        super( 1 );
    }

    public JSFunctionCalls1( Scope scope , String name ){
        super( scope , name , 1 );
    }

    public Object call( Scope scope  , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 0 ); 
            Object p0 = extra == null || extra.length <= 0 ? null : extra[0];
            Object newExtra[] = extra == null || extra.length <= 1 ? null : new Object[ extra.length - 1];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+1];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 2 ); 
            Object newExtra[] = new Object[1 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 1] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 3 ); 
            Object newExtra[] = new Object[2 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 2] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 4 ); 
            Object newExtra[] = new Object[3 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 3] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 5 ); 
            Object newExtra[] = new Object[4 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 4] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 6 ); 
            Object newExtra[] = new Object[5 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 5] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 7 ); 
            Object newExtra[] = new Object[6 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 6] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 8 ); 
            Object newExtra[] = new Object[7 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 7] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 9 ); 
            Object newExtra[] = new Object[8 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 8] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 10 ); 
            Object newExtra[] = new Object[9 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 9] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 11 ); 
            Object newExtra[] = new Object[10 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 10] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 12 ); 
            Object newExtra[] = new Object[11 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 11] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 13 ); 
            Object newExtra[] = new Object[12 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 12] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 14 ); 
            Object newExtra[] = new Object[13 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 13] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 15 ); 
            Object newExtra[] = new Object[14 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 14] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 16 ); 
            Object newExtra[] = new Object[15 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 15] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 17 ); 
            Object newExtra[] = new Object[16 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 16] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 18 ); 
            Object newExtra[] = new Object[17 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 17] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 19 ); 
            Object newExtra[] = new Object[18 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 18] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 20 ); 
            Object newExtra[] = new Object[19 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 19] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 21 ); 
            Object newExtra[] = new Object[20 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 20] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 22 ); 
            Object newExtra[] = new Object[21 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 21] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 23 ); 
            Object newExtra[] = new Object[22 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 22] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 24 ); 
            Object newExtra[] = new Object[23 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            newExtra[22] = p23;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 23] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 25 ); 
            Object newExtra[] = new Object[24 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            newExtra[22] = p23;
            newExtra[23] = p24;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 24] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 26 ); 
            Object newExtra[] = new Object[25 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            newExtra[22] = p23;
            newExtra[23] = p24;
            newExtra[24] = p25;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 25] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 27 ); 
            Object newExtra[] = new Object[26 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            newExtra[22] = p23;
            newExtra[23] = p24;
            newExtra[24] = p25;
            newExtra[25] = p26;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 26] = extra[i];
            return call( scope , p0 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object p27 , Object ... extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 28 ); 
            Object newExtra[] = new Object[27 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p1;
            newExtra[1] = p2;
            newExtra[2] = p3;
            newExtra[3] = p4;
            newExtra[4] = p5;
            newExtra[5] = p6;
            newExtra[6] = p7;
            newExtra[7] = p8;
            newExtra[8] = p9;
            newExtra[9] = p10;
            newExtra[10] = p11;
            newExtra[11] = p12;
            newExtra[12] = p13;
            newExtra[13] = p14;
            newExtra[14] = p15;
            newExtra[15] = p16;
            newExtra[16] = p17;
            newExtra[17] = p18;
            newExtra[18] = p19;
            newExtra[19] = p20;
            newExtra[20] = p21;
            newExtra[21] = p22;
            newExtra[22] = p23;
            newExtra[23] = p24;
            newExtra[24] = p25;
            newExtra[25] = p26;
            newExtra[26] = p27;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 27] = extra[i];
            return call( scope , p0 , newExtra );
    }

    protected ThreadLocal<Integer> _lastStart = new ThreadLocal<Integer>();

}
