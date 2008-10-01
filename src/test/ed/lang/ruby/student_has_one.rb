class StudentHasOne < XGen::Mongo::Base

  collection_name :rubytest_students
  fields :name, :email
  has_one :address

  def initialize(row=nil)
    super
    @address ||= Address.new
  end
end
