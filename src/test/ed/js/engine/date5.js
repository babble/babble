
print( typeof( Date() ) );
print( typeof( new Date() ) );

d = ( new Date(2008, 1, 3, 7, 30, 0, 0) );

// This really sucks; we need to conform to spec or basically anything
print(d.getYear() > 1900? d.getYear()-1900: d.getYear());


