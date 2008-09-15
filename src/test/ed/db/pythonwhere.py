
db = connect( "test" )
t = db["pwhere1"]
t.drop();

t.save( { "a" : 1 } )
t.save( { "a" : 2 } )

def blah1():
    return False

assert( 0 == len( t.find( { "$where" : blah1 } ).toArray() ) )

# ---

def blah2():
    return True


assert( 2 == len( t.find( { "$where" : blah2 } ).toArray() ) )

# ---

def blah3( self ):
    return self.a == 1

assert( 1 == len( t.find( { "$where" : blah3 } ).toArray() ) )
assert( 1 == len( t.find( blah3 ).toArray() ) )


