
/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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

// add ns
ns = new Namespace("foo", "bar");
print( "ns: "+ns );
print( xml.addNamespace( ns ) );
xml.removeNamespace( ns );
//print( xml.addNamespace( "foo" ) );

// multiple pseudo namespaces
xml=<x xmlns:foo="bar"><y>z</y></x>;
xml.setNamespace("foo.foo.foo");
print( xml );
xml.setNamespace("b.b.b.foo");
print( xml );

// copying
abc = <a x="y"><b foo="bar">c<!-- comment --></b></a>;
abcCopy = abc.copy();
print( abc == abcCopy );

abcList = <a>b</a>+<a>c</a>+<a foo="bar">d</a>;
abcCopy = abcList.copy();
print( abcList == abcCopy );
print( abc == abcList );

// namespace stuff
// ANOTHER spidermonkey bug... the following perform to spec and work fine with firebug
//print( xml.inScopeNamespaces().join(",") );
//print( xml.removeNamespace( ns ) );
//print( xml.inScopeNamespaces().length + " " + xml.y.inScopeNamespaces().length );

xml = <x><y>z</y></x>;
xml.setNamespace( ns );
print( xml.inScopeNamespaces().join(",") );
print( xml.removeNamespace( ns ) );
print( xml.inScopeNamespaces().length + " " + xml.y.inScopeNamespaces().length );
xml.setNamespace("hi");
print( xml );
print( xml.name() );
print( xml.inScopeNamespaces().join(",") );
