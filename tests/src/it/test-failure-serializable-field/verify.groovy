File buildLog = new File( basedir, 'build.log' )
assert buildLog.text =~ /(?m)^\[ERROR\] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0$/
