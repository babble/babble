


db = connect( "test1" );
t = db.loop1;

assert.raises(
    function(){
        var a = { z : [] };
        a.z.add( a );
        t.save( a );
    }
);



ta = db.loop1a;
tb = db.loop1b;

ta.drop();
tb.drop();

ta.save( { num : 1 } );
tb.save( { a : ta.findOne() } );
a = ta.findOne();
a.b = tb.findOne();
ta.save( a );
ta.save( ta.findOne() );
tb.save( tb.findOne() );

assert.eq( ta.findOne()._id , tb.findOne().a._id );
assert.eq( tb.findOne()._id , ta.findOne().b._id );

function checkSize(){
    assert.eq( 1 , ta.find().toArray().length );
    assert.eq( 1 , tb.find().toArray().length );
};
checkSize();


// --

a = ta.findOne();
b = a.b;

a.z = 2;
b.z = 3;

ta.save( a );
tb.save( b );

checkSize();

// --

a = ta.findOne();
b = a.b;

a.z = 4;
b.z = 5;

tb.save( b );
ta.save( a );

assert.eq( 5 , tb.findOne().z );
assert.eq( 5 , ta.findOne().b.z );

assert.eq( 4 , ta.findOne().z );
assert.eq( 4 , tb.findOne().a.z );

checkSize();

// --

a = ta.findOne();
b = a.b;
assert( a.z == 4 );
assert( b.z == 5 );

a.z = 6;
b.z = 7;

tb.save( b );

assert.eq( 7 , tb.findOne().z );
assert.eq( 7 , ta.findOne().b.z );

assert.eq( 4 , ta.findOne().z );
assert.eq( 4 , tb.findOne().a.z );

checkSize();

// --


a = ta.findOne();
b = a.b;

a.z = 8;
b.z = 9;

ta.save( a );

assert.eq( 9 , tb.findOne().z );
assert.eq( 9 , ta.findOne().b.z );

assert.eq( 8 , ta.findOne().z );
assert.eq( 8 , tb.findOne().a.z );

checkSize();

// ---

ta = db.loop1aa;
tb = db.loop1ab;
tc = db.loop1ac;

ta.drop(); tb.drop(); tc.drop();

tc.save( { z : 3 } );
tb.save( { z : 2 , c : tc.findOne() } );
ta.save( { z : 1 , b : tb.findOne() } );
c = tc.findOne();
c.a = ta.findOne();
tc.save( c );

assert( 1 , ta.findOne().z );
assert( 2 , ta.findOne().b.z );
assert( 3 , ta.findOne().b.c.z );
assert( 1 , ta.findOne().b.c.a.z );

// --

a = ta.findOne();
a.z = 4;
a.b.z = 5;
a.b.c.z = 6;

ta.save( a );

assert( 4 , ta.findOne().z );
assert( 5 , ta.findOne().b.z );
assert( 6 , ta.findOne().b.c.z );
assert( 4 , ta.findOne().b.c.a.z );


a = ta.findOne();
a.z = 7;
a.b.z = 8;
a.b.c.z = 9;
a.b.c.a = a;

ta.save( a );

assert( 7 , ta.findOne().z );
assert( 8 , ta.findOne().b.z );
assert( 9 , ta.findOne().b.c.z );
assert( 7 , ta.findOne().b.c.a.z );




