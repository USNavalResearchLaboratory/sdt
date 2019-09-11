This directory contains:

sdt3d.hmtl 		simple html page to launch sdt3d
  shiny-red-button.png called by sdt3d.html

sdt3d.jnlp		launches sdt3d 
  gluegen-rt.jnlp	launched by sdt3d.jnlp
  jogl-all.jnlp		launched by sdt3d.jnlp

To serve the app:

1.  Copy these files to the directory location you wish your server to serve from.

2.  Modify the sdt3d.jnlp codebase attribute to point to your server location/directory

	<jnlp codebase="http://192.168.1.6/~ljt/"

3.  Copy all the jar files to the server location. 

To serve all platforms you will need all the native jar files as well (all the "native" jar files in the worldwind directory), e.g.

mbp:Sites ljt$ ls
gdal.jar				jogl-all-natives-windows-i586.jar
gluegen-rt-natives-linux-amd64.jar	jogl-all.jar
gluegen-rt-natives-linux-i586.jar	jogl-all.jnlp
gluegen-rt-natives-macosx-universal.jar	joglutils.jar
gluegen-rt-natives-windows-amd64.jar	libProtolibJni.dylib
gluegen-rt-natives-windows-i586.jar	protolib-jni.jar
gluegen-rt.jar				sdt3d.html
gluegen-rt.jnlp				sdt3d.jar
info.php				sdt3d.jnlp
jogl-all-natives-linux-amd64.jar	shiny-red-button.png
jogl-all-natives-linux-i586.jar		worldwind.jar
jogl-all-natives-macosx-universal.jar	worldwindx.jar
jogl-all-natives-windows-amd64.jar

4.  Sign the jar files.  

a.  generate a keystore file if you don't already have one. (Will prompt for a password)

keytool -genkey -alias <e.g. "nrl"> -keystore <keystore file location, e.g. ~/.keystore)

b.  sign the jars:

jarsigner <jarFileName>.jar <alias>

e.g.

jarsigner sdt3d.jar nrl

5.  You can test that everything works locally with javaws:

javaws sdt3d.jnlp

6.  You may need to change your java security preferences setting to medium on the machine you are browsing from (unless using a key registered with a certification authority) and on osx you may need to change general security settings to "allow applications downloaded from anywhere".


7. start/stop apache web server on osx:

sudo apachectl stop/start

/etc/apache2/users/ljt.conf /Users/ljt/Sites

192.168.1.6/~ljt/sdt3d.html

8. Safari has issues serving jnlp - luck may be better with firefox.
