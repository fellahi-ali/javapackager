#!/bin/bash

if [ -z ${JAVA_HOME} ]; then
  echo "Error: set JAVA_HOME"
  exit
fi

# Argument parsing.
ARGS=()
for i in "$@"; do
    ARGS+=("\"$i\"")
done

JAVA=${JAVA_HOME}/bin/java
JAVAC=${JAVA_HOME}/bin/javac
JAVAPACKAGER=${JAVA_HOME}/bin/javapackager
JMODS=${JAVA_HOME}/jmods
JAR=${JAVA_HOME}/bin/jar
MODULES=${JAVA_HOME}/jmods

rm -rf output
mkdir output

# Generate ICNS file
PLATFORM=`bash ../whichplatform.sh`
ICON="";

if [[ $PLATFORM == "MAC" ]]; then
  mkdir build
  bash ../pngtoicns.sh build/dist/resources/MinesweeperFX.png build/MinesweeperFX
  ICON="-Bicon.icns=build/MinesweeperFX.icns"
fi

# Generate app bundle
eval ${JAVAPACKAGER} $(IFS=$' '; echo "${ARGS[*]}") \
  -deploy \
  -outdir output \
  -singleton \
  -name Test \
  $ICON \
  -native image \
  -BsignBundle=false -Bversion=9.0 \
  -Bmac.dmg.simple=true \
  -srcdir jars \
  -srcfiles MinesweeperFX.jar \
  -Bclasspath=MinesweeperFX.jar \
  -BmainJar=MinesweeperFX.jar \
  -appClass minesweeperfx.MinesweeperFX
