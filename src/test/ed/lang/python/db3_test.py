
import _10gen

db = _10gen.connect( "test" );
t = db.pydb3;
t.drop();

orig= { "name" : "a" , "things" : [ 1 , 2 ] };
_10gen.assert.eq( 2 , len( orig["things"] ) );

orig["things"].append( 3 );
_10gen.assert.eq( 3 , len( orig["things"] ) );

t.save( orig );
after = t.findOne();

_10gen.assert.eq( 3 , len( after["things"] ) );


after["things"].append( 4 );
# THIS BREAKS!
# _10gen.assert.eq( 4 , len( after["things"] ) );  



