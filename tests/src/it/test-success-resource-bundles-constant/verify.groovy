File buildLog = new File( basedir, 'build.log' )
assert buildLog.text =~ /(?m)^\[INFO\] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0$/
