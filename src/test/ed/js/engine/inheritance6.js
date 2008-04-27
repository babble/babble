
function A( name ){
    print( "A" );
    this.name = name;
};


function B( name ){
    this.__proto__.constructor.apply( this , arguments );
    print( "B" );
};

B.prototype = new A();

B.prototype.foo = 17;

b = new B( "eliot" );

print( b.foo );
print( b.name );

