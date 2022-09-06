File buildLog = new File( basedir, 'build.log' )
assert buildLog.text =~ /(?m)^\[INFO\] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0$/
