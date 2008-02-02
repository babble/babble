
function Person( name ){
    this._name = name;
}

Person.prototype.something = function( a ){
    print( a )
};

Person.prototype.printName = function(){
    print( this._name );
}

var p = new Person( "eliot" );


Person.prototype.a = "before";
print( Person.prototype.a );
print( p.a );


Person.prototype.a = "after";
print( p.a );

print( p._name );
p.something("yo");

p.printName();

p.foo = function() { 
    print( this._name );
};
p.foo();


a = {};
a.a = {};
a.a.b = function(){
    this.z = 1;
};

foo = new a.a.b();
print( foo.z );


