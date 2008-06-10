
o = { a : 1 , b : 2 }
assert( 1 == o.a );
o.__preGet = function( z ){
    if ( z == "a" )
        this[z] = null;
};
assert( null == o.a );


var foo = 1;

function A(){

};

a = new A();
assert( null == a.sillya , a.sillya );

A.prototype.__notFoundHandler = function( name ){
    this[name] = foo++;
    return this[name];
};

assert( 1 == a.sillya );
assert( 1 == a.sillya );

assert( 2 == a.asdasdasd );
