
db = connect("test");
t = db.where2;
t.drop();

t.save( { a : 1 } );
t.save( { a : 2 } );
t.save( { a : 3 } );

assert.eq( 1 , t.find( { $where : "function(){ return this.a == 2; }" } ).length() );
assert.eq( 1 , t.find( { $where : "def blah1( self ):\n    return self.a == 2" } ).length() );

assert.eq( 1 , t.find( { $where : "this.a == 2" } ).length() );
assert.eq( 0 , t.find( { $where : "this.a == 4" } ).length() );

t.save( { a : 2 , date : new Date() } );
assert.eq( 2 , t.find( { $where : "this.a == 2" } ).length() );

assert.eq( 1 , t.find( { $where : "this.date && this.date.getDay() == " + ( new Date() ).getDay() } ).length() );
