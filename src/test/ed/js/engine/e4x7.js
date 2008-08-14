// NODES

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

XML.ignoreComments = false;
XML.ignoreProcessingInstructions = false;

var x = "foo";
var y = "bar";
var z = "zap";
var attrname = "attr";

//<?xml-stylesheet href="my.css" type="text/css"?>
var xml = <schools id="0">
  <elementary grades="k6" />
  <middle>{z}</middle>
  <!-- lalalalala -->
  <highschool {attrname}={z}>{y}</highschool>
  <college><{x} {y}={z}>myfoo</{x}><!-- no mr. bond, i expect you to die --></college>
  <!-- that's all for now, folks!' -->
</schools>;

print(xml);

//attribute
print( "attributes" );
print( xml.elementary.attribute( "grades" ) );
print( xml.highschool.attribute( "attr" ) );
print( xml.attribute( "id" ) );
print( xml.college[x].attribute( y ) );
//print( xml.attribute( "err" ) );

print( "----" );
//attributes
print( xml.attributes() );
print( xml.college[x].attributes() );

//comments
//print( xml.comments() );
//print( xml.highschool.comments() );
print( xml.college.comments() );

//processingInstructions
//print( xml.processingInstructions() );

//parent
print( xml.parent() );

xml.foo.bar = "hi";
print( xml.foo.bar.parent() );

//nodeKind
print( xml.college.nodeKind() );
//print( xml.college[x].@a.nodeKind() );

//propertyIsEnumerable
xml.foo[1] = "bye";
xml.foo[2] = "blech";
xml.foo[4] = null;

print(xml);

print( xml.foo.propertyIsEnumerable("0") );
print( xml.foo.propertyIsEnumerable("1") );
print( xml.foo.propertyIsEnumerable("0.1") );
print( xml.foo.propertyIsEnumerable("5") );
print( xml.foo.propertyIsEnumerable("bar") );

// restore default settings
XML.setSettings();