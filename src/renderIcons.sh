#! /bin/bash
inkscape ${1}.svg -z -Ce "../res/drawable-hdpi/${2}.png" -w 72 -h 72
inkscape ${1}.svg -z -Ce "../res/drawable-ldpi/${2}.png" -w 36 -h 36
inkscape ${1}.svg -z -Ce "../res/drawable-mdpi/${2}.png" -w 48 -h 48
inkscape ${1}.svg -z -Ce "../res/drawable-xhdpi/${2}.png" -w 96 -h 96