require 'test/unit'

Dir[File.join(File.dirname(__FILE__), 'test_*.rb')].each { |f| require f }

passed = Test::Unit::AutoRunner.run
raise "a test failed" unless passed
