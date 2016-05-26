#!/bin/sh
# Install protoc on travis-ci
# Thanks to https://github.com/travis-ci/container-example/blob/master/install-protobuf.sh
set -e
# check to see if protobuf folder is empty
if [ ! -d "$HOME/protobuf/lib" ]; then
  wget https://github.com/google/protobuf/releases/download/v2.6.1/protobuf-2.6.1.tar.gz
  tar -xzvf protobuf-2.6.1.tar.gz
  cd protobuf-2.6.1 && ./configure --prefix=$HOME/protobuf && make && make install
else
  echo "Using cached directory."
fi
