# examples
Some small running examples and some short notes on it

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
