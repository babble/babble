
s = "hellothere";

assert(s.slice(0) == s);
assert(s.slice(1) == "ellothere");
assert(s.slice(1,4) == "ell");
assert(s.slice(-1) == "e");
assert(s.slice(1,-1) == "ellother");
assert(s.slice(0,20) == s);
assert(s.slice(-4) == "here");