# Mineshafter Squared - Universal Launcher (4.3.0)
I try to follow [semantic versioning](http://semver.org)

### Contributing
You will need Ant to build and compile the project. You can import the build file into Eclipse (I am using Eclipse for Java EE). Do File => New => Project => Java project from existing Ant buildfile. Make sure to check "link to the buildfile in the file system". If you add libs after importing to eclipse, you'll have to manually add them to the build path as "external Jars"

##### Coding Style
- Indent with tabs
- Fully qualify member access (e.g. `this.foo` and `OuterClass.this.bar()`, but just `STATIC_CONSTANT` and `staticMethod()` are okay)

##### Notes
- To recompile the resources, you must have the Minecraft launcher on `javac`'s classpath. Add "launcher.jar" to "libs" and rename it to "mc-launcher.jar"
- Use `ant -p` to view all tasks and descriptions

### Dependencies
These go under a "libs" folder. You can use the Ant target "downloadlibs" to set these up. This just calls `bash scripts/download_libs.sh`, so you need `bash` on your environment path (Mac and Linux already have this, Windows users look into something like [Cygwin](http://cygwin.com)).

- [SimpleAPI-Java](https://github.com/Raekye/SimpleAPI-Java)
- [Google GSON](https://google-gson.googlecode.com/files/google-gson-2.2.4-release.zip)
- [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/)
- [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
- [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/)
- [Apache Commons Configuration](http://commons.apache.org/proper/commons-configuration/)
- [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/)
- [7Zip](http://7-zip.org) (included in source)

### Credits
- [Ryan](https://github.com/KayoticSully) who works on the Mineshafter Squared [server project](https://github.com/KayoticSully/MineshafterSquared-API)
- [download13](https://github.com/download13) who helped a lot with the [client](https://github.com/download13/Mineshafter-Launcher)
