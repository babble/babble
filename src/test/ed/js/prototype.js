// These are tests for the $A function, which I implemented but then decided
// wasn't very useful to us
/*
a = $A([1, 2, 3]);
assert(a[0] == 1);
assert(a[1] == 2);
assert(a[2] == 3);

o = {0: 1, 4: 5, length: 5};
a = $A(o);

assert(a[0] == 1);
assert(a[4] == 5);
assert(a[2] == null);
*/

// Function.prototype.bind
f = function(a, b, c){
    return [this, a, b, c];
};

newf = f.bind(0, 1, 2);
g = newf(3);
assert(g[0] == 0);
assert(g[1] == 1);
assert(g[2] == 2);
assert(g[3] == 3);

// Make sure we accept empty arguments in either position
f2 = function(){
    return [this].concat(arguments);
};

newf2 = f2.bind(1);
g = newf2();
assert(g.length == 1);
assert(g[0] == 1);

g = newf2(2, 4);
assert(g.length == 3);
assert(g[0] == 1);
assert(g[1] == 2);
assert(g[2] == 4);


C = function(){
    this.x = 1;
};

C.prototype.getX = function(){
    return this.x;
};

C.prototype.getX = C.prototype.getX.wrap(
    function(proceed, factor){
        factor = (factor == null)? 1 : factor;
        return proceed()*factor;
    });

c1 = new C();
assert(c1.getX() == 1);
assert(c1.getX(5) == 5);
c1.x = 6;

assert(c1.getX() == 6);
assert(c1.getX(2) == 12);
