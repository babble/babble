// escape6.js

print( encodeURIComponent( "anasdas12425234542353245225345" ) );
print( encodeURIComponent( " " ) );
print( encodeURIComponent( "%" ) );
print( encodeURIComponent( "@" ) );
print( encodeURIComponent( "`~!@#$%^&*()-_=+" ) );
print( encodeURIComponent( "[]{}\|;:''\"\"/?><.," ) );

print( "!!!" );

print( escape( "anasdas12425234542353245225345" ) );
print( escape( " " ) );
print( escape( "%" ) );
print( escape( "@" ) );
print( escape( "`~!@#$%^&*()-_=+" ) );
print( escape( "[]{}\|;:''\"\"/?><.," ) );

print( "!!!" );

print( encodeURI( "anasdas12425234542353245225345" ) );
print( encodeURI( " " ) );
print( encodeURI( "%" ) );
print( encodeURI( "@" ) );
print( encodeURI( "`~!@#$%^&*()-_=+" ) );
print( encodeURI( "[]{}\|;:''\"\"/?><.," ) );


print( unescape( escape ( "anasdas12425234542353245225345" ) ) );
print( unescape( escape ( " " ) ) );
print( unescape( escape ( "%" ) ) );
print( unescape( escape ( "@" ) ) );
print( unescape( escape ( "`~!@#$%^&*()-_=+" ) ) );
print( unescape( escape ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );

print( decodeURI( encodeURI ( "anasdas12425234542353245225345" ) ) );
print( decodeURI( encodeURI ( " " ) ) );
print( decodeURI( encodeURI ( "%" ) ) );
print( decodeURI( encodeURI ( "@" ) ) );
print( decodeURI( encodeURI ( "`~!@#$%^&*()-_=+" ) ) );
print( decodeURI( encodeURI ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );


print( decodeURIComponent( encodeURIComponent ( "anasdas12425234542353245225345" ) ) );
print( decodeURIComponent( encodeURIComponent ( " " ) ) );
print( decodeURIComponent( encodeURIComponent ( "%" ) ) );
print( decodeURIComponent( encodeURIComponent ( "@" ) ) );
print( decodeURIComponent( encodeURIComponent ( "`~!@#$%^&*()-_=+" ) ) );
print( decodeURIComponent( encodeURIComponent ( "[]{}\|;:''\"\"/?><.," ) ) );

print( "!!!" );
