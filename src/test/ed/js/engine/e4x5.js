// set tests

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

var french = <france><food>fromage</food><food>haricots verts</food><language>francais</language></france>;

french.capital = "paris";

french.food[0] = "pain";
french.food[20] = "pomme";

french.language.accent = "parisian";

print(french);

french.food = "fromage"

french.places = <books><usr>livre</usr><bin>elles</bin></books>

print(french);

xml = <x>y</x>;
xml.foo.@bar = "lalala";
xml.fooy.bar.blah[0] = "fooy!";
xml.bar.foo.bar.foo[0] = <such>a pita</such>;
print( xml );
