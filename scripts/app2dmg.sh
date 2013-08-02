#!/bin/bash

hdiutil create -fs HFS+ -volname "Mineshafter Squared" -srcfolder "dist/mineshaftersquared.app" "dist/mineshaftersquared.dmg"

hdiutil internet-enable -yes "dist/mineshaftersquared.dmg"
