
function A(){
    this.z = 5;
}

A.foo = function(){
    print( "A.foo" );
}

A.prototype.foo = function(){
    print( "A.prototype.foo" );
}

A.foo();
a = new A();
a.foo();
print( a.z );

B = function(){
    this.constructor.__proto__.call( this );
};

B.__proto__ = A;
B.prototype.__proto__ = A.prototype;

B.foo();
b = new B();
b.foo();
print( b.z );
assert( 5 == b.z );
