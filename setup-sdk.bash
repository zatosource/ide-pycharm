#!/usr/bin/env bash

mkdir "python-plugin-sdk"
cd "python-plugin-sdk"
wget -O "python-community-2016.3.163.298.zip" "https://plugins.jetbrains.com/plugin/download?updateId=32326"
rm -rf python
unzip "python-community-2016.3.163.298.zip"
