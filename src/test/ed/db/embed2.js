
db = connect( "test" );
t = db.emebed2;
t.remove( {} );

// ---

A = function(){
    this.name = "hello from A ";
};
A.prototype.z = 11;

Foo = function(){
    this.z = 2;
    this.as = {};
    this.as._dbCons = A;
};

t.setConstructor(Foo);


// ---

f = new Foo();
f.as.a1 = new A();
f.as.a1.y = 2;

assert( f.z == 2 );
assert( f.as.a1 );
assert( f.as.a1.y == 2 );
assert( f.as.a1.z == 11 );

t.save( f );

f = t.findOne();
assert( f.z == 2 );
assert( f.as.a1 );
assert( f.as.a1.y == 2 );
assert( f.as.a1.z == 11 );
