var tests = ["abc", 41, '\'',  "\\", "\"", "\\4", "a\nb", "a\rb", "a\n", "a\\"];

for(var i = 0; i < tests.length; i++){
    var s = tests[i];
    print(tojson(s));
    assert(scope.eval(tojson(s)) == s);
}

// Test tojson of functions, implemented both in JS and in Java

var f = function(){};
assert(typeof scope.eval("("+tojson(f)+")") == "function");
assert(typeof scope.eval(tojson(print)) == "string");

// log is special cased

assert(typeof scope.eval(tojson(log)) == "string");

// Test tojson of Java native objects

// not allowed to use javaCreate -- other object I could create,
// or already created that I could use?
//assert(typeof scope.eval(tojson(javaCreate( "java.util.LinkedList" ))) == "string");
