#!/bin/bash
# Maven launcher workaround for Git-Bash + native Windows JDK.
# The broken apache-maven-3.9.11 has an empty lib/boot, and the maven
# scripts pass MSYS (/c/...) paths to the native Windows java which cannot
# resolve them. We invoke the classworlds launcher directly with Windows paths.
M2U="/c/Users/zhujw2/.m2/wrapper/dists/apache-maven-3.9.9-bin/4nf9hui3q3djbarqar9g711ggc/apache-maven-3.9.9"
M2W=$(cygpath -w "$M2U")
PDW=$(cygpath -w "$PWD")
exec java -classpath "$M2W\\boot\\plexus-classworlds-2.8.0.jar" \
  -Dclassworlds.conf="$M2W\\bin\\m2.conf" \
  -Dmaven.home="$M2W" \
  -Dmaven.multiModuleProjectDirectory="$PDW" \
  -Dlibrary.jansi.path="$M2W\\lib\\jansi-native" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
