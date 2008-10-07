
db = connect("test");
t = db.where2;
t.drop();

t.save( { a : 1 } );
t.save( { a : 2 } );
t.save( { a : 3 } );

assert.eq( 1 , t.find( { $where : "function(){ return this.a == 2; }" } ).length() );
assert.eq( 1 , t.find( { $where : "def blah1( self ):\n    return self.a == 2" } ).length() );
//assert.eq( 1 , t.find( { $where : "def blah2()\n    return obj.a == 2" } ).length() );
