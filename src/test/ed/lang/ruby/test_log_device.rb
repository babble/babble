# Copyright (C) 2008 10gen Inc.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License, version 3, as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

require 'ruby_test'
require 'logger'
require 'xgen/mongo/log_device.rb'

class LoggerTest < RubyTest

  MAX_RECS = 3

  def setup
    super
    $db = connect('test')
    $db.testlogger.drop()
    # Create a log device with a max of MAX_RECS records
    @logger = Logger.new(XGen::Mongo::LogDevice.new('testlogger', :size => 1_000_000, :max => MAX_RECS))
  end

  def teardown
    $db.testlogger.drop()
  end

  # We really don't have to test much more than this. We can trust that Mongo
  # works properly.
  def test_max
    assert_not_nil $db
    assert_equal $db.debugString, XGen::Mongo::LogDevice.connection.debugString
    collection = XGen::Mongo::LogDevice.connection.testlogger
    MAX_RECS.times { |i|
      @logger.debug("test message #{i+1}")
      assert_equal i+1, collection.find().count()
    }

    MAX_RECS.times { |i|
      @logger.debug("test message #{i+MAX_RECS+1}")
      assert_equal MAX_RECS, collection.find().count()
    }
  end

  def test_alternate_connection
    old_db = $db
    alt_db = connect('test-log-device')
    begin
      $db = nil
      XGen::Mongo::LogDevice.connection = alt_db

      logger = Logger.new(XGen::Mongo::LogDevice.new('testlogger', :size => 1_000_000, :max => MAX_RECS))
      logger.debug('test message')

      assert_equal 1, alt_db.testlogger.count()
      rec = alt_db.testlogger.findOne()
      assert_not_nil rec
      assert_match /test message/, rec.msg
    rescue => ex
      fail ex.to_s
    ensure
      $db = old_db
      XGen::Mongo::LogDevice.connection = $db
      alt_db.testlogger.drop()
    end
  end

end
