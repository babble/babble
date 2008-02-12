db = connect( "foo" , "127.0.0.1" );

var t = db.t;

for ( var i=0; i < 999999999.0; i++ ){
    
    o = { ts : new Date( i ) , data : Math.random() };
    t.save( o );
    
    if ( i == 0 )
        t.ensureIndex( { ts : 1} );
    
    assert( t.findOne( { ts : new Date( i ) } ) );

    if ( Math.random() > .99 ){
        var b = t.find().length();
        t.remove( { ts : { $lt : new Date( i - 100000 ) } } );
        var a = t.find().length();
        print( b + "  ->  " + a );
    }
}
