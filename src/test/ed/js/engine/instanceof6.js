
a = 5;
print( a instanceof Number );

a = null;
print( a instanceof Object );

b = null;
a = [ 1 , 2];

print( a instanceof Object );
print( a instanceof Array );

print( ( new Date()) instanceof Date );



A = function(){

};

a = new A();

print( a instanceof A );
