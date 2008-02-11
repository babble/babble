
A = function(){
    this.thingy = 2;
};
A.prototype.junk = 45;
A.prototype.word = 4;

a = new A();
print(a.junk);
print(a.thingy);
print(a.word);
print( a.prototype ? "yes" : "no" );

print( "---" );

B = function(){
    this.thingy = 1;
    this.zzz = "zzz";
};

B.prototype = new A();
//B.prototype.constructor = B;
B.prototype.junk = 54;

b = new B();
print(b.junk);
print(b.thingy);
print(b.word);
print(b.zzz);

print( "---" );

a = new A();
print(a.junk);
print(a.thingy);
print(a.word);
