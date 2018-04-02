#!/bin/bash

openssl aes-256-cbc -K $encrypted_5c15cf265382_key -iv $encrypted_5c15cf265382_iv -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d
gpg --fast-import .travis/codesigning.asc
