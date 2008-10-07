
db = connect("test");
t = db.where1;
t.drop();

t.save( { a : 1 } );
t.save( { a : 2 } );
t.save( { a : 3 } );

assert.eq( 1 , t.find( function(){ return this.a == 2; } ).length() );

n = 2;
assert.eq( 1 , t.find( function(){ return this.a == n; } ).length() );

$f = function(){
    return 2;
}
assert.eq( 1 , t.find( function(){ return this.a == $f(); } ).length() );


$g = function(){
    return n;
}
assert.eq( 1 , t.find( function(){ return this.a == $g(); } ).length() );

assert(t.validate().valid);