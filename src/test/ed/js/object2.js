
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

print( a.silly );

A.prototype.__notFoundHandler = function( name ){
    this[name] = foo++;
    return this[name];
};

print( a.silly );
print( a.silly );
