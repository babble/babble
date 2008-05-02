
function Base(){
    
};

Base.prototype.setName = function( name ){
    this.name = name;
};

// ----

function A(){

};
A.prototype = new Base();
A.prototype.setName( "A" );
print( A.name );

a = new A();
print( a.name );

// ----

function B(){

};
B.prototype = new Base();
B.prototype.setName( "B" );

b = new B();
print( b.name );

a = new A();
print( a.name );
