
db = connect( "test" )
t = db.emebed3;
t.drop();

// ---

function Foo( n ){
    this.n = n;
    this.z = 7;
}

A = function(){
    this.things = [];
    this.things._dbCons = Foo;
}

t.setConstructor( A );

a = new A();
a.things.add( { n : 5 } );
t.save( a );
assert.eq( 1 , t.findOne().things.length );
assert.eq( 5 , t.findOne().things[0].n );
assert.eq( 7 , t.findOne().things[0].z );

// -

t.drop();
assert( ! t.findOne() );

B = function(){
    this.b = true;
    this.things = [];
}

B._dbCons = {
    things : Foo
}

t.setConstructor( B );

b = new B();
b.things.add( { n : 3 } );
t.save( b );

assert( t.findOne().b );
assert.eq( 1 , t.findOne().things.length );
assert.eq( 3 , t.findOne().things[0].n );
assert.eq( 7 , t.findOne().things[0].z );

// ---

t = db.emebed3b;
t.drop();
C = function(){
    this.cc = 9;
    this.b = new B();
}

t.setConstructor( C );
t.save( { zz : 12 , b : { joker : 71 , things : [ { batman : 111 } ] } } );

assert.eq( 9 , t.findOne().cc );
assert.eq( 12 , t.findOne().zz );
assert( t.findOne().b );
assert( t.findOne().b.b );
assert.eq( 71 , t.findOne().b.joker );

assert.eq( 1 , t.findOne().b.things.length );
assert.eq( 111 , t.findOne().b.things[0].batman );
assert.eq( 7 , t.findOne().b.things[0].z );

