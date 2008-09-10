
db = connect( "test" );
t = db.ref9;
t.drop();

thing = { a : 1 };
t.save( { a : thing , b : thing } );

assert.eq( 1 , t.findOne().a.a );
assert.eq( 1 , t.findOne().b.a );

o = t.findOne();
o.a.a = 2;
t.save( o );

assert.eq( 2 , t.findOne().a.a );
assert.eq( 1 , t.findOne().b.a );

thing.b = { c : thing };
try {
    t.save( { d : thing } );
    assert( 0 , "should have said there was a loop" );
}
catch ( e ){
}


