#!/bin/bash

if [ "$OSTYPE" == "linux-gnu" ]; then
  os="linux"
elif [ "$OSTYPE" == "darwin" ]; then
  os="mac"
elif [ "$OSTYPE" == "win32" ]; then
  os="windows"
fi

if [ ! -d ~/.buergerbot ]; then
    mkdir ~/.buergerbot
fi
cd ~/.buergerbot || exit 1

if [ ! -d lib ]; then
  wget "https://gluonhq.com/download/javafx-11-0-2-sdk-$os/" -O javafx.zip
  if ! command -v unzip &>/dev/null; then
    wget "https://oss.oracle.com/el4/unzip/unzip.tar" -O unzip.tar
    tar -xf unzip.tar
    chmod +xf unzip
    unzip javafx.zip
    rm unzip unzip.tar
  else
    unzip javafx.zip
  fi
  rm javafx.zip
  mv javafx-sdk-11.0.2/lib ./lib
  rm javafx-sdk-11.0.2
fi

if [ ! -f ./buergerbot.jar ]; then
  wget "https://github.com/NKb03/buergerbot/releases/download/v1.0/buergerbot.jar" -O buergerbot.jar
fi

java --module-path ./lib --add-modules javafx.controls --add-modules javafx.web --add-opens javafx.web/com.sun.webkit.dom=ALL-UNNAMED -jar ./buergerbot.jar