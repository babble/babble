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

// Here are a bunch of objects whose wrapped forms we want to try to pickle
jsString = "abc";
jsString.attribute = "value";

jsArray = [1, 8, 27];
jsArray.attribute = 'value';

jsObj = {
  one       : 1,
  two       : 8,
  three     : 27,
  attribute : 'value'
};

jsObjectId = ObjectId();

local.src.test.ed.lang.python.pickle1_helper();
