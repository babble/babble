//JSFunctionCalls2.java

package ed.js;

import ed.js.engine.Scope;
public abstract class JSFunctionCalls2 extends JSFunction { 
    public JSFunctionCalls2(){
        super( 2 );
    }

    public JSFunctionCalls2( Scope scope , String name ){
        super( scope , name , 2 );
    }

    public Object call( Scope scope  , Object extra[] ){
            Object p0 = extra == null || extra.length < 0 ? null : extra[0];
            Object p1 = extra == null || extra.length < 1 ? null : extra[1];
            Object newExtra[] = extra == null || extra.length <= 2 ? null : new Object[ extra.length - 2];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+2];
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object extra[] ){
            Object p1 = extra == null || extra.length < 0 ? null : extra[0];
            Object newExtra[] = extra == null || extra.length <= 1 ? null : new Object[ extra.length - 1];
            if ( newExtra != null )
                for ( int i=0; i<newExtra.length; i++ )
                    newExtra[i] = extra[i+1];
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object extra[] ){
            boolean needExtra =  p2 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[1 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 1] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[2 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 2] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[3 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 3] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[4 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 4] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[5 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 5] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[6 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 6] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[7 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 7] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[8 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 8] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[9 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 9] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[10 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 10] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[11 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 11] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[12 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 12] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[13 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 13] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[14 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 14] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[15 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 15] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[16 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 16] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[17 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 17] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[18 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 18] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[19 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 19] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[20 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 20] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[21 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 21] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[22 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                newExtra[21] = p23;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 22] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[23 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                newExtra[21] = p23;
                newExtra[22] = p24;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 23] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[24 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                newExtra[21] = p23;
                newExtra[22] = p24;
                newExtra[23] = p25;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 24] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  ||  p26 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[25 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                newExtra[21] = p23;
                newExtra[22] = p24;
                newExtra[23] = p25;
                newExtra[24] = p26;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 25] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }

    public Object call( Scope scope  , Object p0 , Object p1 , Object p2 , Object p3 , Object p4 , Object p5 , Object p6 , Object p7 , Object p8 , Object p9 , Object p10 , Object p11 , Object p12 , Object p13 , Object p14 , Object p15 , Object p16 , Object p17 , Object p18 , Object p19 , Object p20 , Object p21 , Object p22 , Object p23 , Object p24 , Object p25 , Object p26 , Object p27 , Object extra[] ){
            boolean needExtra =  p2 != null  ||  p3 != null  ||  p4 != null  ||  p5 != null  ||  p6 != null  ||  p7 != null  ||  p8 != null  ||  p9 != null  ||  p10 != null  ||  p11 != null  ||  p12 != null  ||  p13 != null  ||  p14 != null  ||  p15 != null  ||  p16 != null  ||  p17 != null  ||  p18 != null  ||  p19 != null  ||  p20 != null  ||  p21 != null  ||  p22 != null  ||  p23 != null  ||  p24 != null  ||  p25 != null  ||  p26 != null  ||  p27 != null  || ( extra != null && extra.length > 0 ) ;
            Object newExtra[] = needExtra ? new Object[26 + ( extra == null ? 0 : extra.length ) ] : null;
            if ( newExtra != null ){
                newExtra[0] = p2;
                newExtra[1] = p3;
                newExtra[2] = p4;
                newExtra[3] = p5;
                newExtra[4] = p6;
                newExtra[5] = p7;
                newExtra[6] = p8;
                newExtra[7] = p9;
                newExtra[8] = p10;
                newExtra[9] = p11;
                newExtra[10] = p12;
                newExtra[11] = p13;
                newExtra[12] = p14;
                newExtra[13] = p15;
                newExtra[14] = p16;
                newExtra[15] = p17;
                newExtra[16] = p18;
                newExtra[17] = p19;
                newExtra[18] = p20;
                newExtra[19] = p21;
                newExtra[20] = p22;
                newExtra[21] = p23;
                newExtra[22] = p24;
                newExtra[23] = p25;
                newExtra[24] = p26;
                newExtra[25] = p27;
                for ( int i=0; extra != null && i<extra.length; i++ )
                    newExtra[i + 26] = extra[i];
            }
            return call( scope , p0 , p1 , newExtra );
    }


}
