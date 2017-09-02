#!/bin/bash

echo 'Starting server...'
java -cp "./libsAll/*:artifacts/kitek-server-0.1-SNAPSHOT.jar" -Djava.library.path=./native/ MainKt 
