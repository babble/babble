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


f = function(a, b, c){
    return [this, a, b, c];
};

newf = f.bind(0, 1, 2);
g = newf(3);
assert(g[0] == 0);
assert(g[1] == 1);
assert(g[2] == 2);
assert(g[3] == 3);
