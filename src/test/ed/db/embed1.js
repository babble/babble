
db = connect( "test" );
t = db.emebed1;
t.remove( {} );

// ----

A = function(){
    this.name = "hello from A ";
};
A.prototype.z = 11;

B = function(){
    this.name = "hellow from B";
};
B.prototype.z = 12;


Foo = function(){
    this.z = 2;
    this.a = new A();
    this.b = [];
    this.b._dbCons = B;
};

t.setConstructor(Foo);

// ----

f = new Foo();
f.b.push( new B() );
f.a.y = 1;
f.b[0].y = 3;

assert( f.z == 2 );
assert( f.a.y == 1 );
assert( f.b[0].y == 3 );

assert( f.a.z == 11 );
assert( f.b[0].z == 12 );

t.save( f );

f = t.findOne();
assert( f.z == 2 );
assert( f.a.y == 1 );
assert( f.b[0].y == 3 );

assert( f.a.z == 11 );
assert( f.b[0].z == 12 );

