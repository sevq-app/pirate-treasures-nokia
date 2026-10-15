-injars build/raw.jar
-outjars build/preverified.jar
-libraryjars tools/cldcapi11.jar
-libraryjars tools/midpapi20.jar
-dontshrink
-dontoptimize
-dontobfuscate
-dontwarn
-microedition
-target 1.2
-keep public class MaratTreasures extends javax.microedition.midlet.MIDlet {
    public *;
}
