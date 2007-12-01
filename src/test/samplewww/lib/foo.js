print( "loading foo.js<br>" );

function Foo(){
    this.a = 17;
};


function food(){
    print( "food called" );
}

Foo.prototype.go = function(){
    return this.a;
}
