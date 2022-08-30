File buildLog = new File( basedir, 'build.log' )
assert buildLog.text =~ /(?m)^\[INFO\] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0$/
