#!/bin/bash
if [[ -f ".gitignore" ]];then
  git clean -dfX
else
  echo "no .gitignore file found. clean does not success."
fi