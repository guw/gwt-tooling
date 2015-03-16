Note, please use the project update feed to stay on track with updates.

# Changelog #

## 1.5.7 ##
  * Added support for Eclipse 3.5
  * Added an Ant task that can be integrated into Ant scripts to compile modules during project build (requires to run within the same VM as Eclipse)
  * Fixed a few issues since 1.5.1

## 1.5.1 ##
  * Added support for Java 5 generics when generating async service stubs interface ([issue 53](http://code.google.com/p/gwt-tooling/issues/detail?id=53))

## 1.5.0 ##
  * Went back to Java 5 execution environment with 1.5 compliance level to support MacOSX (fixed [issue 48](http://code.google.com/p/gwt-tooling/issues/detail?id=48))
  * Eclipse 3.4 RC3 is now required
  * Verified basic functionality with GWT 1.5.0 RC1

## 1.4.1 ##
  * Added support for Eclipse 3.4M5 and Webtools 3.0M5 (addresses [issue 47](http://code.google.com/p/gwt-tooling/issues/detail?id=47))
  * Added option to project properties to enable compile of GWT modules together with Eclipse Build (addresses [issue 39](http://code.google.com/p/gwt-tooling/issues/detail?id=39))
  * Preferred execution environment is now Java 6, i.e. Eclipse should be started using Java 6. Note, this is not related to the JRE/JDK you are using when developing GWT modules.

## 1.4.0 ##
  * Added support for using gwt-dev-mac.jar on MacOSX as well as using "-XstartOnFirstThread" when launching the GWT Browser (fixes [issue 32](http://code.google.com/p/gwt-tooling/issues/detail?id=32))
  * Additional VM arguments can now be specified on a per project basis (fixes [issue 46](http://code.google.com/p/gwt-tooling/issues/detail?id=46))
  * When deploying GWT modules in web projects only the module folder is now deployed instead of the whole GWT compiler output directory (fixes [issue 44](http://code.google.com/p/gwt-tooling/issues/detail?id=44))

## 1.3.4 ##
  * Fixed GWT compiler output parsing to generate Eclipse error markers when using GWT 1.4

## 1.3.3 ##
  * GWT compiler output is now shown in the Eclipse console

## 1.3.2 ##
  * GWT compile errors during compile & publish are now transfered into Eclipse error markers
  * The style option is no longer set on compile & publish which allows to use the GWT compiler default
  * Generated resources during compile & publish are now marked as derived and read-only resources in the target folder will be asked to become writable
  * Updated to Eclipse 3.3 M6 (we use some internal API... bad, bad, bad, I know ... but it makes life soo easy)
  * Line endigs of generated ".cache.html/xml" and ".nocache.html" files are now automatically converted to the configured line ending style during compile & publish (note, this is a workaround for a [GWT compile issue](http://code.google.com/p/google-web-toolkit/issues/detail?id=876))
  * [list of issues closed in 1.3.2](http://code.google.com/p/gwt-tooling/issues/list?can=1&q=ResolvedIn%3A1.3.2&sort=&colspec=ID+Type+Status+ResolvedIn+Owner+Summary&nobtn=Update)

## 1.3 BETA ##
  * [list of issues closed in 1.3](http://code.google.com/p/gwt-tooling/issues/list?can=1&q=ResolvedIn%3A1.3&sort=&colspec=ID+Type+Status+ResolvedIn+Owner+Summary&nobtn=Update)

## 1.2 BETA ##
  * [list of issues closed in 1.2](http://code.google.com/p/gwt-tooling/issues/list?can=1&q=ResolvedIn%3A1.2&sort=&colspec=ID+Type+Status+ResolvedIn+Owner+Summary&nobtn=Update)

## 1.1 BETA 3 ##
  * [list of issues closed in 1.1 BETA 2 & 3](http://code.google.com/p/gwt-tooling/issues/list?can=1&q=ResolvedIn%3A1.1&sort=&colspec=ID+Type+Status+ResolvedIn+Owner+Summary&nobtn=Update)

## 1.1 BETA 1 ##
  * first release