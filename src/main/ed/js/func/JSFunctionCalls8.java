//JSFunctionCalls8.java

package ed.js.func;

import ed.js.engine.*;
import ed.js.*;
public abstract class JSFunctionCalls8 extends JSFunction { 
    public JSFunctionCalls8(){
        super( 8 );
    }

    public JSFunctionCalls8( Scope scope , String name ){
        super( scope , name , 8 );
    }

    public Object call( Scope scope  , Object extra[] ){
            Object p0 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p1 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p2 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p3 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p4 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p5 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p6 = extra == null || extra.length <= 6 ? null : extra[6];
            Object p7 = extra == null || extra.length <= 7 ? null : extra[7];
            Object newExtra[] = extra == null || extra.length <= 8 ? null : new Object[ extra.length - 8];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+8];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object extra[] ){
            Object p1 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p2 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p3 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p4 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p5 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p6 = extra == null || extra.length <= 5 ? null : extra[5];
            Object p7 = extra == null || extra.length <= 6 ? null : extra[6];
            Object newExtra[] = extra == null || extra.length <= 7 ? null : new Object[ extra.length - 7];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+7];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object extra[] ){
            Object p2 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p3 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p4 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p5 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p6 = extra == null || extra.length <= 4 ? null : extra[4];
            Object p7 = extra == null || extra.length <= 5 ? null : extra[5];
            Object newExtra[] = extra == null || extra.length <= 6 ? null : new Object[ extra.length - 6];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+6];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object extra[] ){
            Object p3 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p4 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p5 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p6 = extra == null || extra.length <= 3 ? null : extra[3];
            Object p7 = extra == null || extra.length <= 4 ? null : extra[4];
            Object newExtra[] = extra == null || extra.length <= 5 ? null : new Object[ extra.length - 5];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+5];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object extra[] ){
            Object p4 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p5 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p6 = extra == null || extra.length <= 2 ? null : extra[2];
            Object p7 = extra == null || extra.length <= 3 ? null : extra[3];
            Object newExtra[] = extra == null || extra.length <= 4 ? null : new Object[ extra.length - 4];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+4];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object extra[] ){
            Object p5 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p6 = extra == null || extra.length <= 1 ? null : extra[1];
            Object p7 = extra == null || extra.length <= 2 ? null : extra[2];
            Object newExtra[] = extra == null || extra.length <= 3 ? null : new Object[ extra.length - 3];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+3];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object extra[] ){
            Object p6 = extra == null || extra.length <= 0 ? null : extra[0];
            Object p7 = extra == null || extra.length <= 1 ? null : extra[1];
            Object newExtra[] = extra == null || extra.length <= 2 ? null : new Object[ extra.length - 2];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+2];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object extra[] ){
            Object p7 = extra == null || extra.length <= 0 ? null : extra[0];
            Object newExtra[] = extra == null || extra.length <= 1 ? null : new Object[ extra.length - 1];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+1];
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object extra[] ){
            boolean needExtra =  p8 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[1 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 1] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[2 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 2] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[3 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 3] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[4 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 4] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[5 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 5] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[6 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 6] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[7 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 7] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[8 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 8] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[9 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 9] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[10 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 10] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[11 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 11] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[12 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 12] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[13 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 13] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[14 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 14] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[15 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 15] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[16 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                newExtra[15] = p23;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 16] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[17 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                newExtra[15] = p23;
                newExtra[16] = p24;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 17] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[18 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                newExtra[15] = p23;
                newExtra[16] = p24;
                newExtra[17] = p25;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 18] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  ||  p26 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[19 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                newExtra[15] = p23;
                newExtra[16] = p24;
                newExtra[17] = p25;
                newExtra[18] = p26;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 19] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object p27 , Object extra[] ){
            boolean needExtra =  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  ||  p26 != null  ||  p27 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[20 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p8;
                newExtra[1] = p9;
                newExtra[2] = p10;
                newExtra[3] = p11;
                newExtra[4] = p12;
                newExtra[5] = p13;
                newExtra[6] = p14;
                newExtra[7] = p15;
                newExtra[8] = p16;
                newExtra[9] = p17;
                newExtra[10] = p18;
                newExtra[11] = p19;
                newExtra[12] = p20;
                newExtra[13] = p21;
                newExtra[14] = p22;
                newExtra[15] = p23;
                newExtra[16] = p24;
                newExtra[17] = p25;
                newExtra[18] = p26;
                newExtra[19] = p27;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 20] = extra[i];
            }
            return call( scope , p0 , p1 , p2 , p3 , p4 , p5 , p6 , p7 , newExtra );
    }


}
