package io.github.jme3.example01;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class Main extends SimpleApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	// Initial colors... when player moves, those will be "switched"
	private ColorRGBA front = ColorRGBA.Green;
	private ColorRGBA right = ColorRGBA.Blue;
	private ColorRGBA bottom = ColorRGBA.Cyan;
	private ColorRGBA left = ColorRGBA.Yellow;
	private ColorRGBA top = ColorRGBA.Magenta;
	private ColorRGBA back = ColorRGBA.Red;

	static {
		// JME uses java.util.logging - bridge to slf4 - see
		// http://www.slf4j.org/legacy.html#jul-to-slf4j
		final java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		final Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			rootLogger.removeHandler(handlers[i]);
		}
		SLF4JBridgeHandler.install();
	}

	public enum PlayerDirection {
		FORWARD, BACK, LEFT, RIGHT, NONE
	}
	
	private PlayerDirection direction = PlayerDirection.NONE;
	private Spatial player;
	private Quaternion rotQuat = new Quaternion();
	boolean moving = false;

	float alpha = 0;
	float cubeRotationSpeed = 5f; // good values: 0.5f slow, 1 normal, 2 fast
	private float fromAngle = 0;
	private float toAngle = 90;
	private Vector3f axis = null;

	private Vector3f playerPosition = new Vector3f(0, 1, 0);
	private Vector3f playerPositionTarget = new Vector3f();

	private final float yOffset = 1.15f;
	private final float xzScale = 2.1f;// 4.5f;

	private int flyCamMoveSpeed = 30;
	private int flyCamZoomSpeed = 10;

	public static void main(String args[]) {
		LOGGER.info("main()...");
		Main app = new Main();
		app.start();
	}
	

	@Override
	public void simpleInitApp() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f)); // otherwise black

		Material m = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		float x = 1f;
		float y = 0.1f;
		float z = 1f;

		for (int i = 0; i <= 9; i++) {
			for (int j = 0; j <= 9; j++) {
				Spatial obj = new Geometry("cube", new Box(x, y, z));
				obj.setShadowMode(ShadowMode.CastAndReceive);
				obj.setMaterial(m);
				rootNode.attachChild(obj);

				obj.setLocalTranslation(xzScale * i, 0, xzScale * j);

				rootNode.attachChild(obj);
			}
		}

		player = new Geometry("cube", new Box(1, 1, 1));
		rotate(PlayerDirection.LEFT);

		flyCam.setMoveSpeed(flyCamMoveSpeed);
		flyCam.setZoomSpeed(flyCamZoomSpeed);

		DirectionalLight light = new DirectionalLight();
		light.setDirection(new Vector3f(-1, -2, -3));
		light.setDirection(new Vector3f(1, -2, 3));
		rootNode.addLight(light);

		cam.setLocation(new Vector3f(-5, 35, -5));
		cam.lookAt(new Vector3f(8, 0, 8), Vector3f.UNIT_Y);

		defineInputMappings();

		LOGGER.info("Usage:");
		LOGGER.info(" W/A/S/D: Change scene");
		LOGGER.info(" Up/Left/Down/Right: Change scene");
		LOGGER.info(" I/J/K/L: Move Cube");
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		if (!moving) {
			alpha = 0;
		}
		if (tpf < 1 && moving) {
			final float delta = Math.min(tpf, 1 / 30f);
			alpha += delta * cubeRotationSpeed;
			float angle = fromAngle + alpha * (toAngle - fromAngle);

			rotQuat.fromAngleAxis(angle * FastMath.DEG_TO_RAD, axis);
			player.setLocalRotation(rotQuat);

			// lerp
			Vector3f tmpV = new Vector3f(playerPosition.x, playerPosition.y, playerPosition.z);
			tmpV.x += alpha * (playerPositionTarget.x - playerPosition.x);
			tmpV.y += alpha * (playerPositionTarget.y - playerPosition.y);
			tmpV.z += alpha * (playerPositionTarget.z - playerPosition.z);

			tmpV.x *= xzScale;
			tmpV.y = yOffset;
			tmpV.z *= xzScale;

			player.setLocalTranslation(tmpV);

			if (angle > toAngle && direction == PlayerDirection.RIGHT) {
				moving = false;
				rotate(direction);
				playerPosition = playerPositionTarget;
			} else if (angle < toAngle && direction == PlayerDirection.LEFT) {
				moving = false;
				rotate(direction);
				playerPosition = playerPositionTarget;
			} else if (angle > toAngle && direction == PlayerDirection.BACK) {
				moving = false;
				rotate(direction);
				playerPosition = playerPositionTarget;
			} else if (angle < toAngle && direction == PlayerDirection.FORWARD) {
				moving = false;
				rotate(direction);
				playerPosition = playerPositionTarget;
			}
		}
	}

	private void rotate(PlayerDirection rot) {
		if (rot == PlayerDirection.RIGHT) {
			ColorRGBA temp = top;
			top = back;
			back = bottom;
			bottom = front;
			front = temp;
		} else if (rot == PlayerDirection.LEFT) {
			ColorRGBA temp = front;
			front = bottom;
			bottom = back;
			back = top;
			top = temp;
		} else if (rot == PlayerDirection.BACK) {
			ColorRGBA temp = bottom;
			bottom = left;
			left = top;
			top = right;
			right = temp;
		} else if (rot == PlayerDirection.FORWARD) {
			ColorRGBA temp = right;
			right = top;
			top = left;
			left = bottom;
			bottom = temp;
		}

		fromAngle = 0;

		Material matPlayer = new Material(assetManager, "MyColorBoxShader.j3md");
		matPlayer.setColor("TopColor", top);
		matPlayer.setColor("FrontColor", front);
		matPlayer.setColor("RightColor", right);
		matPlayer.setColor("BackColor", back);
		matPlayer.setColor("LeftColor", left);
		matPlayer.setColor("BottomColor", bottom);

		player.removeFromParent();

		player = new Geometry("cube", new Box(1, 1, 1));
		rootNode.attachChild(player);

		player.setLocalTranslation(playerPositionTarget.x * xzScale, yOffset, playerPositionTarget.z * xzScale);

		player.setMaterial(matPlayer);
		Vector3f localTranslation = player.getLocalTranslation();
		player.setLocalTranslation(localTranslation); // for refreshing?
	}

	private void defineInputMappings() {
		inputManager.addMapping("I", new KeyTrigger(KeyInput.KEY_I));
		ActionListener i = (name, keyPressed, tpf) -> {
			if (!keyPressed) {
				moving = true;
				toAngle = -90;
				direction = PlayerDirection.FORWARD;
				axis = Vector3f.UNIT_Z;

				playerPositionTarget.set(playerPosition.x, playerPosition.y, playerPosition.z);
				playerPositionTarget = playerPositionTarget.add(1, 0, 0);
			}
		};
		inputManager.addListener(i, "I");

		inputManager.addMapping("J", new KeyTrigger(KeyInput.KEY_J));
		ActionListener j = (name, keyPressed, tpf) -> {
			if (!keyPressed) {
				moving = true;
				toAngle = -90;
				direction = PlayerDirection.LEFT;
				axis = Vector3f.UNIT_X;

				playerPositionTarget.set(playerPosition.x, playerPosition.y, playerPosition.z);
				playerPositionTarget = playerPositionTarget.add(0, 0, -1);
			}
		};
		inputManager.addListener(j, "J");

		inputManager.addMapping("K", new KeyTrigger(KeyInput.KEY_K));
		ActionListener k = (name, keyPressed, tpf) -> {
			if (!keyPressed) {
				moving = true;
				toAngle = 90;
				direction = PlayerDirection.BACK;
				axis = Vector3f.UNIT_Z;

				playerPositionTarget.set(playerPosition.x, playerPosition.y, playerPosition.z);
				playerPositionTarget = playerPositionTarget.add(-1, 0, 0);
			}
		};
		inputManager.addListener(k, "K");

		inputManager.addMapping("L", new KeyTrigger(KeyInput.KEY_L));
		ActionListener l = (name, keyPressed, tpf) -> {
			if (!keyPressed) {
				moving = true;
				toAngle = 90;
				direction = PlayerDirection.RIGHT;
				axis = Vector3f.UNIT_X;
				playerPositionTarget.set(playerPosition.x, playerPosition.y, playerPosition.z);
				playerPositionTarget = playerPositionTarget.add(0, 0, 1);

			}
		};
		inputManager.addListener(l, "L");
	}
}
