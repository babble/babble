require 'address'

class Score < XGen::Mongo::Base

  field :course_name, :grade

  def passed?
    @grade >= 2.0
  end

  def to_s
    "#{@course_name}: #@grade"
  end

end

class Student < XGen::Mongo::Base

  collection_name :rubytest_students
  fields :name, :email, :num_array
  has_one :address
  has_many :scores, :class_name => 'Score'

  def initialize(row=nil)
    super
    @address ||= Address.new
  end
end
