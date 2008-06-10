
db = connect( "test" )



db.getCollectionPrototype().doSomething = function(){
    lastName = this.getName();
    return 7;
}

assert( 7 == db.foo.doSomething() );
assert( "foo" == lastName );

assert( db.nutty.getClass().toString().match( /Collection/ ) );
db.nutty = 123;
assert( 123 == db.nutty );

