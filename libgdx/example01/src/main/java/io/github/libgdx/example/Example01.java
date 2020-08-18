package io.github.libgdx.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

/**
 * Example from
 * 
 * https://github.com/xoppa/blog/blob/master/tutorials/src/com/xoppa/blog/libgdx/g3d/basic3d/step3/Basic3DTest.java
 * 
 * Extensions:
 * 
 * - Cube has difference colors on its faces
 * - Added a cubemap (gdx-gltf) - not the best images, but for a normal photo its ok...
 * 
 *  Please note: I'm new to libGDX and GDX-glTF - so some things could be implemented better.
 *  Feel free to make a pull request, but please keep the example as simple as possible.
 *  
 *  https://libgdx.badlogicgames.com/
 *  https://github.com/mgsx-dev/gdx-gltf
  */
public class Example01 extends ApplicationAdapter {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1920;
		config.height = 1080;
		new LwjglApplication(new Example01(), config);
	}

	private Environment environment;
	private PerspectiveCamera cam;
	private CameraInputController camController;
	private ModelBatch modelBatch;
	private Model model;
	private ModelInstance instance;

	private SceneManager sceneManager;
	
	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		model = createCubeModel();
		instance = new ModelInstance(model);

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
		
		// Extension: Cubemap
		Scene scene = new Scene(model);
		sceneManager = new SceneManager();
		sceneManager.addScene(scene);
		sceneManager.camera = cam;
		Cubemap environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"gltf/textures/environment/", ".png", EnvironmentUtil.FACE_NAMES_FULL);
		sceneManager.setAmbientLight(1f);
		SceneSkybox skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);
	}

	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
	}

	@Override
	public void render() {
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.end();
		
		sceneManager.update(Gdx.graphics.getDeltaTime());
		sceneManager.render();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}
	
	private Model createCubeModel() {
		ModelBuilder modelBuilder = new ModelBuilder();
		float d = 0.5f;
		int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
				| VertexAttributes.Usage.TextureCoordinates;

		Color front = Color.BLACK;
		Color right = Color.YELLOW;
		Color bottom = Color.BLUE;
		Color left = Color.RED;
		Color top = Color.GREEN;
		Color back = Color.CYAN;
		
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = 0.7f;
		modelBuilder.begin();
		modelBuilder
				.part("front", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(front)))
				.rect(-d, -d, -d, -d, d, -d, d, d, -d, d, -d, -d, 0, 0, -1);
		modelBuilder
				.part("back", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(back)))
				.rect(-d, d, d, -d, -d, d, d, -d, d, d, d, d, 0, 0, 1);
		modelBuilder
				.part("bottom", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(bottom)))
				.rect(-d, -d, d, -d, -d, -d, d, -d, -d, d, -d, d, 0, -1, 0);
		modelBuilder
				.part("top", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(top)))
				.rect(-d, d, -d, -d, d, d, d, d, d, d, d, -d, 0, 1, 0);
		modelBuilder
				.part("left", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(left)))
				.rect(-d, -d, d, -d, d, d, -d, d, -d, -d, -d, -d, -1, 0, 0);
		modelBuilder
				.part("right", GL20.GL_TRIANGLES, attr,
						new Material(blendingAttribute, ColorAttribute.createDiffuse(right)))
				.rect(d, -d, -d, d, d, -d, d, d, d, d, -d, d, 1, 0, 0);
		
		return modelBuilder.end();
	}
}
