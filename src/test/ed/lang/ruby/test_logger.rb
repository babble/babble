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
    run_js <<EOS
db = connect('test');
db.testlogger.drop();
EOS
    # Create a log device with a max of MAX_RECS records
    @logger = Logger.new(XGen::Mongo::LogDevice.new('testlogger', :size => 1_000_000, :max => MAX_RECS))
  end

  def teardown
    run_js 'db.testlogger.drop();'
  end

  # We really don't have to test much more than this. We can trust that Mongo
  # works properly.
  def test_max
    MAX_RECS.times { |i|
      @logger.debug("test message #{i+1}")
      assert_equal i+1, $db.testlogger.find().count()
    }

    MAX_RECS.times { |i|
      @logger.debug("test message #{i+MAX_RECS+1}")
      assert_equal MAX_RECS, $db.testlogger.find().count()
    }
  end

end
