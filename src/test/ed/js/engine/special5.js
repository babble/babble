
f = function(){
    this.y = true;
};

g = new f();
f.prototype.x = 5;
f.prototype.z = 111;

print(g.__proto__.x);
print(g.__proto__ == f.prototype);

b = function(){
    
};
b.prototype.z = 1;

print( g.__proto__.z );
g.__proto__ = b.prototype;
print( g.__proto__.z );



