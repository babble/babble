
import types

db = connect( "test" );
t = db.pythonnumbers
t.drop()

thing = { "a" : 5 , "b" : 5.5 }
assert( type( thing["a"] ) == types.IntType );
assert( type( thing["b"] ) == types.FloatType );

t.save( thing )

thing = t.findOne()
assert( type( thing["b"] ) == types.FloatType );
assert( type( thing["a"] ) == types.IntType );
