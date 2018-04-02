#!/bin/bash

exec mvn -B -V -e -U --settings .travis/settings.xml "$@"
