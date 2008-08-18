
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

XML.ignoreComments = true;
xml=<blargh><blah id="52" prop="some prop"><something id="1" snakes="in_planes">grr</something><!-- here is a comment --><else id="2">arg</else></blah><blah>2</blah><blah>3</blah><!-- and here is another comment --></blargh>;

print(xml.blah[0].@*);
print(xml.blah[0].@id);
print(xml.blah.something.@*);

print(xml.blah.*);
print(xml.blah[1]);
print(xml.blah[1].*);

delete xml.blah[0];

print(xml);

xml.blah[0].@id = "4";
xml.blah[1].@foo = "bar";
xml.name = "me";
xml.name.@attr="foo";
xml.@name = "me2"

print(xml);

xml.blah[1].@foo = "zap";
print(xml.blah[1].@foo);

delete xml.blah;

delete xml.@name;
delete xml.name.@attr;
print(xml);
