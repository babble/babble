
function Person( name ){
    this._name = name;
}

Person.prototype.something = function( a ){
    print( a )
};

var p = new Person( "eliot" );

Person.prototype.a = "before";
print( p.a );
Person.prototype.a = "after";
print( p.a );

print( p._name );
p.something("yo");




