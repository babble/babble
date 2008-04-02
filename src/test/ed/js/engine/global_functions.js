
assert(!isNaN(1));
assert(isNaN(NaN));
assert(isNaN(parseInt("hello", 10)));
assert(!isNaN(parseInt("10",10)));
