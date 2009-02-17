//JSFunctionCalls14.java

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
public abstract class JSFunctionCalls14 extends JSFunction { 
    public JSFunctionCalls14(){
        super( 14 );
    }

    public JSFunctionCalls14( Scope scope , String name ){
        super( scope , name , 14 );
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
            Object newExtra[] = extra == null || extra.length <= 14 ? null : new Object[ extra.length - 14];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+14];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 13 ? null : new Object[ extra.length - 13];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+13];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 12 ? null : new Object[ extra.length - 12];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+12];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 11 ? null : new Object[ extra.length - 11];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+11];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 10 ? null : new Object[ extra.length - 10];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+10];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 9 ? null : new Object[ extra.length - 9];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+9];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 8 ? null : new Object[ extra.length - 8];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+8];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
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
            Object newExtra[] = extra == null || extra.length <= 7 ? null : new Object[ extra.length - 7];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+7];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 8 ); 
            Object p8 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p9 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p10 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p11 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p12 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p13 = extra == null || extra.length <= 5 ? null : extra[5];
            Object newExtra[] = extra == null || extra.length <= 6 ? null : new Object[ extra.length - 6];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+6];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 9 ); 
            Object p9 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p10 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p11 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p12 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p13 = extra == null || extra.length <= 4 ? null : extra[4];
            Object newExtra[] = extra == null || extra.length <= 5 ? null : new Object[ extra.length - 5];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+5];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 10 ); 
            Object p10 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p11 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p12 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p13 = extra == null || extra.length <= 3 ? null : extra[3];
            Object newExtra[] = extra == null || extra.length <= 4 ? null : new Object[ extra.length - 4];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+4];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 11 ); 
            Object p11 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p12 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p13 = extra == null || extra.length <= 2 ? null : extra[2];
            Object newExtra[] = extra == null || extra.length <= 3 ? null : new Object[ extra.length - 3];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+3];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 12 ); 
            Object p12 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p13 = extra == null || extra.length <= 1 ? null : extra[1];
            Object newExtra[] = extra == null || extra.length <= 2 ? null : new Object[ extra.length - 2];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+2];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 13 ); 
            Object p13 = extra == null || extra.length <= 0 ? null : extra[0];
            Object newExtra[] = extra == null || extra.length <= 1 ? null : new Object[ extra.length - 1];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+1];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 15 ); 
            Object newExtra[] = new Object[1 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 1] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 16 ); 
            Object newExtra[] = new Object[2 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 2] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 17 ); 
            Object newExtra[] = new Object[3 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 3] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 18 ); 
            Object newExtra[] = new Object[4 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 4] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 19 ); 
            Object newExtra[] = new Object[5 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 5] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 20 ); 
            Object newExtra[] = new Object[6 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 6] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 21 ); 
            Object newExtra[] = new Object[7 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 7] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 22 ); 
            Object newExtra[] = new Object[8 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 8] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 23 ); 
            Object newExtra[] = new Object[9 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 9] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 24 ); 
            Object newExtra[] = new Object[10 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            newExtra[9] = p23;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 10] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 25 ); 
            Object newExtra[] = new Object[11 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            newExtra[9] = p23;
            newExtra[10] = p24;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 11] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 26 ); 
            Object newExtra[] = new Object[12 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            newExtra[9] = p23;
            newExtra[10] = p24;
            newExtra[11] = p25;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 12] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object [] extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 27 ); 
            Object newExtra[] = new Object[13 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            newExtra[9] = p23;
            newExtra[10] = p24;
            newExtra[11] = p25;
            newExtra[12] = p26;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 13] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object p27 , Object ... extra ){
            if ( _lastStart.get() == null ) _lastStart.set( 28 ); 
            Object newExtra[] = new Object[14 + ( extra == null ? 0 : extra.length ) ];
            newExtra[0] = p14;
            newExtra[1] = p15;
            newExtra[2] = p16;
            newExtra[3] = p17;
            newExtra[4] = p18;
            newExtra[5] = p19;
            newExtra[6] = p20;
            newExtra[7] = p21;
            newExtra[8] = p22;
            newExtra[9] = p23;
            newExtra[10] = p24;
            newExtra[11] = p25;
            newExtra[12] = p26;
            newExtra[13] = p27;
            for ( int i=0; extra != null && i<extra.length; i++ )
                newExtra[i + 14] = extra[i];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , p8 , p9 , p10 , p11 , p12 , p13 , newExtra );
    }

    protected ThreadLocal<Integer> _lastStart = new ThreadLocal<Integer>();

}
