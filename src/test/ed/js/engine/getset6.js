
o = {
    a:7,

    get b() { 
        return this.a+1; 
    }

    ,
    
    set c(x) { 
        this.a = x/2; 
    }

};

print( o.b );


foo = function(){
    this.a = 4;
}

foo.prototype = {
    get b(){
        return this.a+1; 
    }
};

f = new foo();
print( f.b );

