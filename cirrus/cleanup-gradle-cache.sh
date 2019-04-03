#/bin/sh

rm -rf ~/".gradle/caches/$GRADLE_VERSION/"
find ~/.gradle/caches/ -name "*.lock" -type f -delete
