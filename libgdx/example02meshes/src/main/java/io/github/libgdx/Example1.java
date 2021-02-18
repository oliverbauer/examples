package io.github.libgdx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import io.github.libgdx.MeshUtil.MOD;

public class Example1 extends ApplicationAdapter {
	private static final boolean renderGrid = false;

	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1920;
		config.height = 1080;
		new LwjglApplication(new Example1(), config);
	}

	private Environment environment;
	private PerspectiveCamera cam;
	private CameraInputController camController;
	private ModelBatch modelBatch;
	private ModelInstance grid;

	private ModelInstance board;
	private ModelInstance board2;
	private ModelInstance rotatingPyramidHead;

	private PointLight pointLight;
	private SpotLight spotLight;

	@Override
	public void create() {
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(-5f, 10f, -5f);
		cam.lookAt(5, 0, 5);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		board = createBoard();
		board2 = createBoard2();
		rotatingPyramidHead = createBoard3();

		grid = createGrid();

		pointLight = new PointLight();
		pointLight.set(Color.RED, 8f, 1f, 8f, 10f);
		environment.add(pointLight);

		Vector3 pos = new Vector3(0, 10, 0);
		Vector3 dir = new Vector3(0, 5, 0);
		spotLight = new SpotLight().set(Color.BLUE, pos, dir, 100f, 100, 1);
		environment.add(spotLight);

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render() {
		camController.update();

		float delta = Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 0f); // Background r,g,b,a... without: black
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// "A rotation around a point is the same as translating to that point, rotating
		// and then translating back."
		// Cf. https://stackoverflow.com/questions/26656334/libgdx-set-rotation-point-3d
		rotatingPyramidHead.transform
			.translate(4, 0, 12) // Positon of the pyramid
			.rotate(0, 0.1f, 0, 45 * delta)
			.translate(-4, 0, -12); // Positon of the pyramid

		modelBatch.begin(cam);
		if (renderGrid) {
			modelBatch.render(grid, environment);
		}
		modelBatch.render(board, environment);
		modelBatch.render(board2, environment);
		modelBatch.render(rotatingPyramidHead, environment);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
	}

	private ModelInstance createBoard() {
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = .9f;

		ModelBuilder builder = new ModelBuilder();
		builder.begin();

		Material material = createDiffuseMaterialFromImage(java.awt.Color.CYAN);

		MeshPartBuilder mpb = builder
				.part("box", GL20.GL_TRIANGLES,
						VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
								| VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates,
						material);

		float width = 1;
		float height = 0.1f;
		float depth = 1;
		for (int x = 0; x <= 10; x++) {
			MeshUtil first = new MeshUtil(x, 0, 0, width, height, depth).finialize(mpb);
			MeshUtil secon = new MeshUtil(x, 1, 1, width, height, depth).alignLeftSideWithRightSideOf(first)
					.finialize(mpb);
			MeshUtil third = new MeshUtil(x, 1, 2, width, height, depth).alignLeftSideWithRightSideOf(secon)
					.finialize(mpb);
			MeshUtil fourt = new MeshUtil(x, 0, 3, width, height, depth).alignLeftSideWithRightSideOf(third)
					.finialize(mpb);
			new MeshUtil(x, 0, 4, width, height, depth).alignLeftSideWithRightSideOf(fourt).finialize(mpb);
			MeshUtil sixth = new MeshUtil(x, 0, 5, width, height, depth).modify(MOD.THREE).finialize(mpb);
			new MeshUtil(x, 0, 6, width, height, depth).alignLeftSideWithRightSideOf(sixth).finialize(mpb);
			new MeshUtil(x, 0, 7, width, height, depth).finialize(mpb);
		}

		new MeshUtil(0, 0, 9, width, height, depth).finialize(mpb);
		new MeshUtil(0, 0, 12, width, height, depth).modify(MOD.ONE).finialize(mpb);
		new MeshUtil(0, 0, 15, width, height, depth).modify(MOD.TWO).finialize(mpb);
		new MeshUtil(0, 0, 17, width, height, depth).modify(MOD.THREE).finialize(mpb);

		new MeshUtil(4, 0, 9, width, 2, depth).finialize(mpb);
		new MeshUtil(4, 0, 12, width, 2, depth).modify(MOD.ONE).finialize(mpb);
		new MeshUtil(4, 0, 15, width, 2, depth).modify(MOD.TWO).finialize(mpb);
		new MeshUtil(4, 0, 17, width, 2, depth).modify(MOD.THREE).finialize(mpb);

		ModelInstance mi = new ModelInstance(builder.end());
		mi.transform.translate(0.5f, 0.5f, 0.5f);
		mi.materials.get(0).set(TextureAttribute.createDiffuse(createTexture(java.awt.Color.LIGHT_GRAY)));
		return mi;
	}

	private ModelInstance createBoard2() {
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = .9f;

		ModelBuilder builder = new ModelBuilder();
		builder.begin();

		Material material = createDiffuseMaterialFromImage(java.awt.Color.CYAN);

		MeshPartBuilder mpb = builder
				.part("box", GL20.GL_TRIANGLES,
						VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
								| VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates,
						material);

		float width = 1;
		float height = 4;
		float depth = 1;

		width = 1;
		new MeshUtil(9, 0, 9, width, height, depth).finialize(mpb);
		new MeshUtil(9, 0, 12, width, height, depth).modify(MOD.ONE).finialize(mpb);
		new MeshUtil(9, 0, 15, width, height, depth).modify(MOD.TWO).finialize(mpb);
		new MeshUtil(9, 0, 17, width, height, depth).modify(MOD.THREE).finialize(mpb);

		ModelInstance mi = new ModelInstance(builder.end());
		mi.transform.translate(0.5f, 2, 0.5f);

		mi.materials.get(0).set(TextureAttribute.createDiffuse(createTexture(java.awt.Color.LIGHT_GRAY)));

		return mi;
	}

	private ModelInstance createBoard3() {
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = .9f;

		ModelBuilder builder = new ModelBuilder();
		builder.begin();

		Material material = createDiffuseMaterialFromImage(java.awt.Color.CYAN);

		MeshPartBuilder mpb = builder
				.part("box", GL20.GL_TRIANGLES,
						VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
								| VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates,
						material);

		float width = 1;
		float height = 1;
		float depth = 1;

		new MeshUtil(4, 0, 12, width, height, depth).modify(MOD.TWO).finialize(mpb);

		ModelInstance mi = new ModelInstance(builder.end());
		mi.transform.setTranslation(0.5f, 2, 0.5f);
		mi.materials.get(0).set(TextureAttribute.createDiffuse(createTexture(java.awt.Color.LIGHT_GRAY)));

		return mi;
	}

	private ModelInstance createGrid() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		final float GRID_MIN = 0f;
		final float GRID_MAX = 10f;
		final float GRID_STEP = 1f;
		MeshPartBuilder builder = modelBuilder.part("grid", GL20.GL_LINES, Usage.Position, new Material());
		builder.setColor(Color.GREEN);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		builder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position, new Material());
		builder.setColor(Color.RED);
		// x
		builder.line(0, 0, 0, 20, 0, 0);
		builder.line(0, 1, 0, 20, 1, 0);
		builder.line(0, 2, 0, 20, 2, 0);

		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 20, 0);
		builder.setColor(Color.BLUE);
		// z
		builder.line(0, 0, 0, 0, 0, 20);
		builder.line(0, 1, 0, 0, 1, 20);
		builder.line(0, 2, 0, 0, 2, 20);
		return new ModelInstance(modelBuilder.end());
	}

	private Texture createTexture(java.awt.Color c) {
		BufferedImage bufferedImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();

		g2d.setColor(java.awt.Color.BLACK);
		g2d.fillRect(0, 0, 80, 80);

		g2d.setColor(c);
		g2d.fillRect(1, 1, 78, 78);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "png", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] byteArray = baos.toByteArray();
		Pixmap mask = new Pixmap(byteArray, 0, byteArray.length);

		return new Texture(mask);
	}

	private Material createDiffuseMaterialFromImage(java.awt.Color c) {
		Texture t = createTexture(c);
		return new Material(TextureAttribute.createAmbient(t));
	}

}