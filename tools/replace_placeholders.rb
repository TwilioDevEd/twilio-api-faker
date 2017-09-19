Dir.glob('api-descriptors/**/*.json') do |file|
  content = File.read(file)
  default = 'XXaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
  phone = '+18001234567'
  values_to_replace = {
    'US': default,
    'AT': default,
    '+987654321': phone,
    'phone_number': phone,
    'key': default,
    '100': default,
    'sidOrUniqueName': default,
    'sid': default,
    'identity': default
    }
  values_to_replace.each do |to_be_replaced, replacer|
    content.gsub! /(?!\/#{Regexp.escape(to_be_replaced)}aa)\/#{Regexp.escape(to_be_replaced)}/, '/' + replacer
  end
  file = File.open(file, 'w')
  file.write(content)
end
