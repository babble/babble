var tests = ["abc", 41, '\'',  "\\", "\""];

for(var i = 0; i < tests.length; i++){
    var s = tests[i];
    print(tojson(s));
    assert(scope.eval(tojson(s)) == s);
}
