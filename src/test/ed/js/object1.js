a = Object.extend({}, {foo: 1, bar: 2});
assert(a.foo == 1);
assert(a.bar == 2);

b = Object.extend(a, {baz: 8});

assert(b.foo == 1);
assert(b.bar == 2);
assert(b.baz == 8);
assert(a.baz == 8);

c = Object.extend(a, {foo: 5});

assert(c.foo == 5);
assert(a.foo == 5);
assert(c.bar == 2);

d = Object.extend("hi", {newfunc: function(){ return this[0]; } });
assert(d.newfunc() == 'h');

e = "yo";
assert(e.newfunc == null);

a = {a: 1, b: 2, c: 3};
vals = Object.values(a);
for(var n = 1; n <= 3; n++){
    i = vals.indexOf(n);
    assert(i != -1);
    vals.splice(i, 1);
}

assert(vals.length == 0);

