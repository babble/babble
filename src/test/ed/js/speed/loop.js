function go() { 

var o = {
            s : "hello",
            t : 1
         };

var d = new Date();
var j = 0;
var k = "s";

for (var i = 0; i < 10000; i++) { 
    j = j + i;

    if (i % 10) { 
	j = j / 2;
    }

    k = o.s;
    o.s = o.s + 1;
    o.t++;
}

var e = new Date();

print(e-d);
}


go();
go();
