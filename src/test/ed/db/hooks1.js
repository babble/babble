
db = connect( "test" );
t = db.hooks1;

function Foo(){
    this.a = 1;
};

Foo.prototype.postLoad = function(){
    this.c = 3;
}

t.setConstructor( Foo );
t.save( { n : 2 } );

assert.eq( 1 , t.findOne().a );
assert.eq( 2 , t.findOne().n );
assert.eq( 3 , t.findOne().c );
