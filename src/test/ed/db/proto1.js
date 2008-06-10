
db = connect( "test" )



db.getCollectionPrototype().doSomething = function(){
    lastName = this.getName();
    return 7;
}

assert( 7 == db.foo.doSomething() );
assert( "foo" == lastName );

