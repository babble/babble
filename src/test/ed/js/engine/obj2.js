
var o = Object();
o.a = "eliot";
o["b"] = "horowitz";

print( o["a"] + " " + o.b );

o.c = Object();
o.c.d = "food";
print( o["c"]["d"] );

for(var key in o){
    print(key);
}
