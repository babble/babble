
function Foo(){
    
}

Foo.foo = function(){
    return 1;
}

Foo.prototype.bar = function(){
    return 2;
}


assert.eq( 1 , Foo.foo() );
f = new Foo();
assert.eq( 2 , f.bar() );
assert.eq( 1 , f.foo() );
