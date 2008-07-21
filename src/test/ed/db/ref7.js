
db = connect( "test" );

ta = db.ref7a;
tb = db.ref7b;

ta.drop();
tb.drop();

aID = ta.save( { num : 1 } )._id;
assert( aID );

tb.save( { num : 2 , thing : ta.findOne() } );


b = tb.findOne();
assert( 1 == b.thing.num );

a = ta.findOne();
a.num = 3;
ta.save( a );
assert( 3 == ta.findOne().num );

tb.save( b );
assert( 3 == ta.findOne().num , ta.findOne().num );


// ----

b = tb.findOne();
assert( 3 == b.thing.num );

a = ta.findOne();
a.num = 4;
ta.save( a );
assert( 4 == ta.findOne().num );

b.thing.abc = 1;
tb.save( b );
assert( 3 == ta.findOne().num );


// ----

a = ta.findOne();
a.a = [ 1 , 2 ];
ta.save( a );

b = tb.findOne();
assert( 3 == b.thing.num );
assert( 2 == b.thing.a.length );

prev = b.thing.a.hashCode();
b.thing.a.push( 3 );
assert( 3 == b.thing.a.length );
assert( prev != b.thing.a.hashCode() , "hash didn't change : " + b.thing.a.hashCode() + " == " + prev );
assert( b.thing.isDirty() );

tb.save( b );

assert( tb.find().toArray().length == 1 );
b = tb.findOne();
assert( 3 == b.thing.num );
assert( 3 == b.thing.a.length , b.thing.a.length );
