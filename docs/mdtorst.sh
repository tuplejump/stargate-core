#!/bin/bash
    # replace the + to # chars
    sed -i -r 's/^([+]{4})\s/#### /' $1
    sed -i -r 's/^([+]{3})\s/### /' $1
    sed -i -r 's/^([+]{2})\s/## /' $1
    sed -i -r 's/^([+]{1})\s/# /' $1
    sed -i -r 's/(\[php\])/<?php/' $1

    # convert markdown to reStructured Text
    pandoc -f markdown -t rst $1 > query.rst
