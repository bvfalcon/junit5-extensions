File buildLog = new File( basedir, 'build.log' )
assert buildLog.text.contains( '@Test executed. constructorParameter value: test-value-483' )
assert buildLog.text.contains( '@Test executed. constructorParameter value: test-value-295' )
assert buildLog.text.contains( '@Test executed. constructorParameter value: test-value-542' )
assert buildLog.text.contains( '@Test executed. constructorParameter value: test-value-385' )
assert 4 == buildLog.text.count( '@BeforeEach callback success' )
assert 4 == buildLog.text.count( '@AfterEach callback success' )
assert 1 == buildLog.text.count( '@BeforeAll callback success' )
assert 1 == buildLog.text.count( '@AfterAll callback success' )