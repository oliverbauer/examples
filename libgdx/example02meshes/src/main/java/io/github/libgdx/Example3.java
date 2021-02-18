package io.github.libgdx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;

public class Example3 extends ApplicationAdapter {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1920;
		config.height = 1080;
		
		config.width = 1280;
		config.height = 768;
		
		new LwjglApplication(new Example3(), config);
	}
	
	private Environment environment;
	private PerspectiveCamera cam;
	private ModelBatch modelBatch;
	
	@Override
	public void create() {
		environment = new Environment();

		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
		DirectionalLight directionalLight = new DirectionalLight();
		directionalLight.set(
			new Color(1.0f, 0.8f, 0.8f, 0.7f), 
			-1f, 
			-2.8f,
			-0.5f
		);
		environment.add(directionalLight);
		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(-3f, 7f, -3f);
		cam.lookAt(0, 0, 0);
		cam.update();
		
		
		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new CameraInputController(cam));
		
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		
		float h = 0.05f;
		float w = 0.5f;
		float d = 0.5f;
		
		List<List<MeshUtil2.COORD>> list1 = new ArrayList<>();
		List<List<MeshUtil2.COORD>> list2 = new ArrayList<>();
		List<List<MeshUtil2.COORD>> list3 = new ArrayList<>();
		
		int five = 25;
		int ten = 20;
		
		for (int i=0; i<=ten; i++) {
			List<MeshUtil2.COORD> vorne = new ArrayList<>();
			for (int j=0; j<=five; j++) {
				vorne.add(new MeshUtil2.COORD(new Vector3(j*w, 0*h, i*d), new Vector3(j*w, 1*h, i*d), new Vector3(j*w, 0*h, (i+1)*d), new Vector3(j*w, 1*h, (i+1)*d)));
			}
			list1.add(vorne);
			list2.add(vorne);
			list3.add(vorne);
		}

		// Area1
		Random r = new Random(System.currentTimeMillis());
		for (int i=1; i<=100; i++) {
			
			int row = r.nextInt(ten);
			int column = r.nextInt(five);
			
			float height = r.nextFloat();
			
			list1.get(row).get(column).topBack.y += height; // der mittlere | | |
			list1.get(row+1).get(column).topFront.y += height;
		}
		for (Model m : MeshUtil2.getModels(list1)) {
			modelInstances.add(new ModelInstance(m));
		}
		
		// Area 2
		for (int i=1; i<=ten; i++) {
			for (int j=0; j<=five; j++) {
				float scale = 0.1f;
				float yoffset = 2.5f;

				float y = (float)(Math.pow(scale*i, 3) - 3*scale*i + Math.pow(j*scale, 2));
				y += yoffset;
				list2.get(i-1).get(j).topBack.y = y;
				list2.get(i).get(j).topFront.y = y;
				
				list2.get(i-1).get(j).bottomBack.y = y-h;
				list2.get(i).get(j).bottomFront.y = y-h;
			}
		}
		for (Model m : MeshUtil2.getModels(list2)) {
			ModelInstance modelInstance = new ModelInstance(m);
			modelInstance.transform.translate(10, 5, 5);
			modelInstances.add(modelInstance);
		}

		for (int i=1; i<=ten; i++) {
			for (int j=0; j<=five; j++) {
				float y = 0.05f*(float)(j*Math.sin(i) - i*Math.cos(j));
				
				list3.get(i-1).get(j).topBack.y = y;
				list3.get(i).get(j).topFront.y = y;
				
				list3.get(i-1).get(j).bottomBack.y = y-h;
				list3.get(i).get(j).bottomFront.y = y-h;
			}
		}
		for (Model m : MeshUtil2.getModels(list3)) {
			ModelInstance modelInstance = new ModelInstance(m);
			modelInstance.transform.translate(20, 5, 5);
			modelInstances.add(modelInstance);
		}
	}
	List<ModelInstance> modelInstances = new ArrayList<>();
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 0f); // Background r,g,b,a... without: black
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		
		for (ModelInstance mi : modelInstances) {
			modelBatch.render(mi, environment);
		}
		
		modelBatch.end();
	}
	
	@Override
	public void dispose() {
		modelBatch.dispose();
	}
}
