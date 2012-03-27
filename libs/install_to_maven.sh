#!/bin/bash
#installs the libs to your local maven repo
mvn install:install-file -Dfile=libGoogleAnalytics.jar -DgroupId=com.google.android.apps -DartifactId=analytics -Dversion=1.3.1 -Dpackaging=jar
mvn install:install-file -Dfile=signpost-core-1.2.1.1.jar -DgroupId=oauth.signpost -DartifactId=signpost-core -Dversion=1.2.1.1 -Dpackaging=jar
mvn install:install-file -Dfile=android-support-v4-r4-googlemaps.jar -DgroupId=android.support -DartifactId=compatibility-v4 -Dversion=r4 -Dpackaging=jar
