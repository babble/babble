
// tests for "deep" changes, like addition and loops
// addition
xml = <a>b</a>+<a>b</a>;
print( xml );
print( "---" );

xml = <a>b</a>+<c>d</c>+<a>b</a>;
print( xml );
print( "---" );

print( xml + <yo></yo>);
print( typeof xml );
print( typeof (xml + "hi") );

// appending
x2 = <x><a>b</a></x>;
x2.a += <b>c</b>;
x2.a += "hi";

print(x2);

x2 += "hi";
print( typeof x2 );

// for
xml=<x><a>b</a><a>3</a><a>gah</a><b>foo</b></x>
for( var v in xml.a ) {
    print( v+": "+xml.a[v] );
}

