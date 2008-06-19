
db = connect( "test" )
t = db.transient1;

t.remove( {} );

o = {};
o.a = 1;
o.b = 2;
o._transientFields = [ "b" ];

t.save( o );
o = t.findOne();
assert( 1 == o.a );
assert( o.b == null , "not null when it should be: [" + o.b + "]" );

t.remove( {} );

Foo = function(){
    this.a = 3;
    this.b = 4;
}

Foo.prototype._transientFields = [ "b" ];

f = new Foo();
t.save( f );
o = t.findOne();
assert( 3 == o.a );
assert( o.b == null , "not null when it should be: [" + o.b + "]" );


