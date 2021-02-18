# examples
Some small running examples and some short notes on it

## libgdx-mesh-experiments

![Alt text](/libgdx/example02meshes/src/main/resources/screenshot.jpg?raw=true "Screenshot")

## orientdb-tinkerpop

Downloads some actor informations of a few movies from (german) [Wikipedia](https://de.wikipedia.org/wiki/Wikipedia:Hauptseite) (parsed with [jsoup](https://jsoup.org/)). Some temporary yaml-files will be created (loaded with [Jackson](https://github.com/FasterXML/jackson)) that will serve as input to [OrientDB](https://www.orientdb.org/) (submitted with [Apache TinkerPop](https://tinkerpop.apache.org/)).

The following Screenshot (OrientDB Studio) depicts a shortest path between two movies:

![Image of shortestpath](https://github.com/oliverbauer/examples/blob/master/orientdb/tinkerpop/src/main/screenshots/shortestPath.jpg)

Howto run: Start OrientDB
```
...orientdb-3.1.6/bin$ ./server.sh
```
and run the main class
```
examples/orientdb/tinkerpop$ mvn exec:java -Dexec.mainClass=io.github.orientdb.example.Main
```
You maybe need to create a demodb and use admin/admin as username/password - i can't remember. Otherwise change Main.java for your needs.

Check out the readme.txt for some simple queries and how to remove nodes.

## libgdx-example01
An example for [stackoverflow-question](https://stackoverflow.com/questions/63446137/3d-background-in-libgdx-without-using-blender-fbx-conv)

*originalinput* is wxh: 5184x3888
Imagemagick:
> convert originalinput.jpg -geometry 624x originalSmall.jpg
*originalSmall* is wxh: 648x468

![Image of originalSmall](https://github.com/oliverbauer/examples/blob/master/images/example01/originalSmall.jpg)

I created a "mirror image" of *originalSmall* using the following source code:
https://dyclassroom.com/image-processing-project/how-to-create-a-mirror-image-in-java

![Image of mirrored](https://github.com/oliverbauer/examples/blob/master/images/example01/originalSmallMirror.jpg)

I downloaded a project called "Cylinder to Skybox" (easy to use jar-file) which creates 6 images for me
https://sourceforge.net/projects/cylindertoskybox/

![Image of Cylinder To Skybox](https://github.com/oliverbauer/examples/blob/master/images/example01/cylinderToSkybox.jpg)

Screenshot of a private project (*libgdx-example01* only contains a single colored cube)

![Image of originalSmall](https://github.com/oliverbauer/examples/blob/master/images/example01/screenshot.jpg)
