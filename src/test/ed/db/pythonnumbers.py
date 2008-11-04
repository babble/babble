
import _10gen
import types
import _10gen

db = _10gen.connect( "test" );
t = db.pythonnumbers
t.drop()

thing = { "a" : 5 , "b" : 5.5 }
assert( type( thing["a"] ) == types.IntType );
assert( type( thing["b"] ) == types.FloatType );

t.save( thing )

thing = t.findOne()
assert( type( thing["b"] ) == types.FloatType );
assert( type( thing["a"] ) == types.IntType );

t.drop();

t.save( { "a" : 1 , "b" : 1.0 } );

assert( t.findOne() );
assert( t.findOne( { "a" : 1 } ) );
assert( t.findOne( { "b" : 1.0 } ) );

assert( not t.findOne( { "b" : 2.0 } ) );

assert( t.findOne( { "a" : 1.0 } ) );
assert( t.findOne( { "b" : 1 } ) );


