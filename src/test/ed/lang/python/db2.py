import _10gen

db = _10gen.connect( "test" );
t = db.pydb2;
t.drop();

t.save( { "name" : "eliot" , "foo" : [ 1 , 2 ] } );
x = t.findOne();
_10gen.assert.eq( "eliot" , x.name );
_10gen.assert.eq( 2 , len( x.foo ) );
_10gen.assert.eq( 1 , x.foo[0] );
_10gen.assert.eq( 2 , x.foo[1] );
