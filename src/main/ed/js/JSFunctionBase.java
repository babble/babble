// JSFunctionBase.java

package ed.js;

public abstract class JSFunctionBase extends JSInternalFunctions { 
    public JSFunctionBase( int num ){
        _num = num;
    }

    final int _num;

    public Object call( ){
        if ( _num == 0 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 0 )
            throw new RuntimeException( "too many params" );
        return call(null);
    
    }
    public Object call( Object p0 ){
        if ( _num == 1 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 1 )
            throw new RuntimeException( "too many params" );
        return call(p0,null);
    
    }
    public Object call( Object p0 ,  Object p1 ){
        if ( _num == 2 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 2 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ){
        if ( _num == 3 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 3 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ){
        if ( _num == 4 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 4 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ){
        if ( _num == 5 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 5 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ){
        if ( _num == 6 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 6 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ){
        if ( _num == 7 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 7 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ){
        if ( _num == 8 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 8 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ){
        if ( _num == 9 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 9 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ){
        if ( _num == 10 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 10 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ){
        if ( _num == 11 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 11 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ){
        if ( _num == 12 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 12 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ){
        if ( _num == 13 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 13 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ){
        if ( _num == 14 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 14 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ){
        if ( _num == 15 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 15 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ){
        if ( _num == 16 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 16 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ){
        if ( _num == 17 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 17 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ){
        if ( _num == 18 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 18 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ){
        if ( _num == 19 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 19 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ){
        if ( _num == 20 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 20 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ){
        if ( _num == 21 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 21 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ){
        if ( _num == 22 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 22 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ){
        if ( _num == 23 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 23 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ){
        if ( _num == 24 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 24 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ){
        if ( _num == 25 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 25 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ,  Object p25 ){
        if ( _num == 26 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 26 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,p25,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ,  Object p25 ,  Object p26 ){
        if ( _num == 27 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 27 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,p25,p26,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ,  Object p25 ,  Object p26 ,  Object p27 ){
        if ( _num == 28 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 28 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,p25,p26,p27,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ,  Object p25 ,  Object p26 ,  Object p27 ,  Object p28 ){
        if ( _num == 29 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 29 )
            throw new RuntimeException( "too many params" );
        return call(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12,p13,p14,p15,p16,p17,p18,p19,p20,p21,p22,p23,p24,p25,p26,p27,p28,null);
    
    }
    public Object call( Object p0 ,  Object p1 ,  Object p2 ,  Object p3 ,  Object p4 ,  Object p5 ,  Object p6 ,  Object p7 ,  Object p8 ,  Object p9 ,  Object p10 ,  Object p11 ,  Object p12 ,  Object p13 ,  Object p14 ,  Object p15 ,  Object p16 ,  Object p17 ,  Object p18 ,  Object p19 ,  Object p20 ,  Object p21 ,  Object p22 ,  Object p23 ,  Object p24 ,  Object p25 ,  Object p26 ,  Object p27 ,  Object p28 ,  Object p29 ){
        if ( _num == 30 )
            throw new RuntimeException( "this should not happen" );
        if ( _num < 30 )
            throw new RuntimeException( "too many params" );
        throw new RuntimeException( "fuck" );
     }

}
