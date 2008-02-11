
db = connect( "foo" , "127.0.0.1" );

var s = "asdasdasdasds";
while ( s.length < 1024 * 20 )
    s += s;

var t = db.t;

for ( var i=0; i != 1000000; i++ ){
    a = { data : s , name : "asd" + i } 
    t.save( a );

    b = t.findOne( a._id );
    assert( b );
    assert( a.data == b.data );
    assert( a.name == b.name );

    b = t.findOne( { name : a.name} );
    assert( b );
    assert( a.data == b.data );
    assert( a.name == b.name );

    t.ensureIndex( { name : 1 } );

    if ( i > 100 ){
        var l = "asd" + ( Math.random() * i ).toFixed(0);
        assert( t.findOne( { name : l } ) );
    }
}
