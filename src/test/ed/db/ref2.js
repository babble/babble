
db = connect( "test" );
ta = db.ref2a;
ta.remove( {} );
tb = db.ref2b;
tb.remove( {} );

// ----

A = function(){
    this.name = "hello from A ";
};
A.prototype.z = 11;
ta.setConstructor(A);

B = function(){
    this.z = 2;
    this.a = new A();
};
tb.setConstructor(B);

// ----

b = new B();
b.a.y = 1;

assert( b.z == 2 );
assert( b.a.y == 1 );
assert( b.a.z == 11 );

ta.save( b.a );
tb.save( b );

b = tb.findOne();
assert( b.z == 2 );
assert( b.a.y == 1 );
assert( b.a.__constructor__ );
assert( b.a.z == 11 );

exit();
