#!/bin/bash

until $(curl --output /dev/null --silent --fail "$1"); do
    printf '.'
    sleep 3
done