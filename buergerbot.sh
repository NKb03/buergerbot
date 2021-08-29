#!/bin/bash

. ~/.bashrc

locate_javafx_sdk() {
  echo "Do you have the JavaFX SDK installed? ([y]es/[n]o)"
  read -r j
  if [ "$j" != "y" ]; then
    if [ "$OSTYPE" == "linux-gnu" ]; then
      os="linux"
    elif [ "$OSTYPE" == "darwin" ]; then
      os="mac"
    elif [ "$OSTYPE" == "win32" ]; then
      os="windows"
    fi
    wd=$PWD
    if [ ! -d ~/.javafx ]; then
        mkdir ~/.javafx
    fi
    cd ~/.javafx || exit 1
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
    JAVAFX_SDK="$HOME/.javafx/javafx-sdk-11.0.2"
    cd "$wd" || exit 1
  else
    echo "Where is your JavaFX SDK installed?"
    read -r JAVAFX_SDK
    if [ ! -f $"$JAVAFX_SDK/lib/javafx.controls.jar" ]; then
      echo "JavaFX SDK not recognized."
      exit 1
    fi
  fi
  echo "export JAVAFX_SDK=$JAVAFX_SDK" >>~/.bashrc
  # shellcheck disable=SC1090
  . ~/.bashrc
}
echo "JavaFX SDK: $JAVAFX_SDK"

if [ -z "$JAVAFX_SDK" ]; then
  locate_javafx_sdk
elif [ ! -f $"$JAVAFX_SDK/lib/javafx.controls.jar" ]; then
  echo "Your JavaFX SDK is not recognized."
  locate_javafx_sdk
fi

java --module-path "$JAVAFX_SDK/lib" --add-modules javafx.controls --add-modules javafx.web --add-opens javafx.web/com.sun.webkit.dom=ALL-UNNAMED -jar buergerbot.jar
