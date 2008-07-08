/* test indexing where the key is an embedded object.
 */

core.db.db();

db = connect( "test" );
t = db.counttest;

t.remove( {} );
assert( 0 == t.count() );

t.save( { name : "a" } );
assert( 1 == t.count() );
assert( 1 == t.find().count() );


t.save( { name : "b" } );
assert( 2 == t.find().count() );

assert( 1 == t.find( { name : "b" } ).count() );
assert( 1 == t.find( { name : "a" } ).count() );
//assert( 0 == t.find( { name : "c" } ).count() );
assert( 2 == t.find( { name : "c" } ).count() );

