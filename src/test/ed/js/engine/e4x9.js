
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

//NAMESPACES
message = <soap:Envelope soap:encodingStyle="http://encoding.com/" xmlns:soap="http://blahblah.org">
  <soap:Body>
    <m:GetLastTradePrice xmlns:m="http://mycompany.com/">
      <symbol>DIS</symbol>
    </m:GetLastTradePrice>
  </soap:Body>
</soap:Envelope>;

suds = new Namespace( "http://blahblah.org" );
vader = new Namespace( "http://mycompany.com/" );
print( suds );

print( message.name().uri );
print( message.suds::Body.name() )

print( message.suds::Body.vader::GetLastTradePrice.symbol );

//default xml namespace = new Namespace( "bar2" );
level1 = new Namespace("bar2");
level2 = new Namespace("bar");
xml = <x xmlns:foo="bar"><foo2:y xmlns:foo2="bar2"><foo:z>sha nanana get a job</foo:z></foo2:y></x>;

print( xml.level1::y.level2::z )

//setLocalName
//setName
//setNamespace

//inScopeNamespaces


//localName
//name
//namespace
//namespaceDeclarations
//removeNamespace
