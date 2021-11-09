#!/bin/bash

currentDir=$(pwd)
if [[ "${currentDir}" != */specs ]]; then
  cd specs
fi
bundle exec jekyll serve ${*}

# echo "Goto: http://127.0.0.1:4000/"
