
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

print( message.name().uri );

//set default namespace = new Namespace("http://www.mynamespace.com/");

//setLocalName
//setName
//setNamespace

//inScopeNamespaces


//localName
//name
//namespace
//namespaceDeclarations
//removeNamespace
