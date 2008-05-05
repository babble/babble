
Foo = function(){
    this.z = 11;
};

Foo.prototype.fun = {
    z : 12 , 
    go : function(){
        print( this.z );
    }
};

f = new Foo();
f.fun.go();
