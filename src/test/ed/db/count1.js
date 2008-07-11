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
assert( 0 == t.find( { name : "c" } ).count() );

t=db.counttest2;
t.remove( {} );

for(x=0; x < 100; x++ )
    t.save({i:x, foo: "aaaa"});

for( pass=0; pass<2; pass++ ) { 

    assert( 100 == t.count() );

    assert( t.find({i:3}).count() == 1 );
    assert( t.find({i:3333}).count() == 0 );
    assert( t.find({foo:"aaaa"}).count() == 100 );

    assert( t.find({i:{$gte:98}}).count() == 2 );

}
