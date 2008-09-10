
db = connect( "test" )
t = db.emebed3;
t.drop();

t2 = db.emebed4;
t2.drop();

// ---

function Foo( n ){
    this.n = n;
    this.z = 7;
}

C = function(x, y){
    this.x = x;
    this.y = y;
};

C.prototype.meth1 = function(){
    return this.x + this.y;
};

t2.setConstructor( C );

A = function(foo){
    this.things = [];
    this.stuff = foo;
}

t.setConstructor( A );

c1 = new C(2, 3);
c2 = new C(8, 12);
t2.save(c1);
t2.save(c2);

a = new A();
a.things.add( c1 );
a.stuff = c2;
t.save( a );

var x = t.findOne();

assert.eq( 1 , x.things.length );
assert.eq( 5 , x.things[0].meth1() );
assert.eq( 20 , x.stuff.meth1() );
