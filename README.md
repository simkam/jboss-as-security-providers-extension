# JBoss AS Java security provider extension

Extension for registering Java Security Providers in JBoss AS at runtime.

Current version supports only SunPKCS11 provider.

## How to get it

You need to have [git](http://git-scm.com/) installed

	$ git clone git://github.com/kwart/jboss-as-security-providers-extension.git

## How to build it

You need to have [Maven](http://maven.apache.org/) installed

	$ cd jboss-as-security-providers-extension
	$ mvn clean install

## How to install it

Copy the produced module to the JBoss AS modules (set correct path to `JBOSS_HOME`):

	$ JBOSS_HOME=/home/test/jboss-as
	$ cp -R target/module/* "$JBOSS_HOME/modules"
	
Until `sun.jdk` package exports are fixed in the JBoss AS you have to edit file `$JBOSS_HOME/modules/sun/jdk/main/module.xml` manually. Add this line to exports:

	<path name="sun/security/pkcs11"/>

## How to use it

Use the CLI -  jboss-cli.sh (or .bat). Add the AS extension and register the security provider(s): 

	/extension=org.jboss.as.security.providers:add
	/subsystem=security-providers:add
	/subsystem=security-providers/sunpkcs11=NSSfips:add(attributes=[("nssLibraryDirectory"=>"/opt/tests/nss/lib"),("nssSecmodDirectory"=>"/opt/tests/nss/fipsdb"),("nssModule"=>"fips")])

Check JBoss AS console (or log files) if no error occures during the Security Provider registration. 