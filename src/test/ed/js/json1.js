
assert( tojson( { a : true , b : true }));

assert( { a : true }.toJSON().contains( "true" ) );
assert.eq( { a : true }.toJSON() , { a : true , b : function(){} }.toJSON() );

return;
// These are some checks to see how our JSON looks

var o = { a: 1, b: 2 };
var s = tojson( o );

print(s);

var o = { a: "1", b: "2" };
var s = tojson( o );

print(s);

var o = { a : { b: 1 } };
var s = tojson( o );

print(s);

var o = { a : 1 , b : 2 , c : 3 , d : 4 , e : 5 , f : 6 , g : 7 , h : 8 , i : 9,
    j : 10 , k : 11 , l : 12 , m : 13 , n : 14 , o : 15 , p : 16 , q : 17 };
var s = tojson( o );

print(s);


var o = { a : ['b' , 'c'] };
var s = tojson( o );

print(s);

var o = { a : ['b', { lorem : "ipsum" } , 'dolor'], sit: 'amet', consectetur : [ { 'adipisci' : 'velit' }, { 'sed' : 'eius' }, {'mod' : 'tempor' , 'incidunt' : 'ut labore', 'et' : 'dolore' } ] };
var s = tojson( o );

print(s);
