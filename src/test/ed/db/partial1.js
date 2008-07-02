
db = connect( "test" );

t = db.partial1;
t.drop();

t.save( { a : 1 , b : 2 } );

o = t.find( {} , { a : 1 } )[0];
assert( o );
assert( o.a == 1 );

o.a = 2;
try {
    t.save( o );
    assert( 0 , "shouldn't let me save it" );
}
catch ( e ){
    assert( e.toString().match( /partial/i ) );
}

o = t.find( {} , { a : 1 } )[0];
assert( o );
assert( o.a == 1 );

// --

o = t.find()[0];
assert( o );
assert( o.a == 1 );

o.a = 2;
t.save( o );

o = t.find( {} , { a : 1 } )[0];
assert( o );
assert( o.a == 2 );


