#!/bin/bash

exec mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec
