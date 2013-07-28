# Mineshafter Squared - Universal Launcher (4.3.0)
I try to follow [semantic versioning](http://semver.org)

### Contributing
You will need Ant to build and compile the project. You can import the build file into Eclipse (I am using Eclipse for Java EE). Do File => New => Project => Java project from existing Ant buildfile. Make sure to check "link to the buildfile in the file system". If you add libs after importing to eclipse, you'll have to manually add them to the build path as "external Jars"

##### Notes
- To recompile the resources, you must have the Minecraft launcher on javac's classpath. Add "launcher.jar" to "libs" and rename it to "mc-launcher.jar"
- The "main" task does not recompile the resources. The "all" task does

### Dependencies
These go under a "libs" folder. There is a download script to set these up (`bash scripts/download_libs.sh`). Alternatively, you can use the Ant target "downloadlibs" which just calls that script. (You need `bash` on your environment path)
- [SimpleAPI-Java](https://github.com/Raekye/SimpleAPI-Java)
- [Google GSON](https://google-gson.googlecode.com/files/google-gson-2.2.4-release.zip)
- [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/)
- [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
- [Apache Commons CLI](http://commons.apache.org/proper/commons-cli/)
- [Apache Commons Configuration](http://commons.apache.org/proper/commons-configuration/)
- [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/)

### Credits
- [Ryan](https://github.com/KayoticSully) who works on the Mineshafter Squared [server project](https://github.com/KayoticSully/MineshafterSquared-API)
- [download13](https://github.com/download13) who helped a lot with the [client](https://github.com/download13/Mineshafter-Launcher)
