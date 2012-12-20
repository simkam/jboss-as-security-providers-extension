# JBoss AS Java security provider extension

Extension for registering Java Security Providers in JBoss AS at runtime.

Current version supports only SunPKCS11 provider.

## How to get it working

Until package exports are fixed on the JBoss AS you have to edit file `[jboss-as7-inst]/modules/sun/jdk/main/module.xml` manually. Add this line:

	<path name="sun/security/pkcs11"/>

Add your security providers using JBoss CLI:

	/subsystem=security-providers:add
	/subsystem=security-providers/sunpkcs11=NSSfips:add(attributes=[("nssLibraryDirectory"=>"/opt/tests/nss/lib"),("nssSecmodDirectory"=>"/opt/tests/nss/fipsdb"),("nssModule"=>"fips")])
