#!/bin/bash

currentDir=$(pwd)
if [[ "${currentDir}" != */specs ]]; then
  cd specs
fi
bundle install
bundle add webrick
