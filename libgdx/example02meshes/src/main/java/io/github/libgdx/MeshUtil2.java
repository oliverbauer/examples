package io.github.libgdx;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class MeshUtil2 {
	public static class COORD {
		Vector3 bottomFront;
		Vector3 topFront;
		Vector3 bottomBack;
		Vector3 topBack;
		
		public COORD(Vector3 bottomFront, Vector3 topFront, Vector3 bottomBack, Vector3 topBack) {
			this.bottomBack = bottomBack;
			this.bottomFront = bottomFront;
			this.topFront = topFront;
			this.topBack = topBack;
		}
	}
	
	public static List<Model> getModels(List<List<MeshUtil2.COORD>> list) {
		List<Model> models = new ArrayList<>();
		
		for (int zs=1; zs<=list.size()-2; zs++) {
			List<COORD> layer = list.get(zs);

			
			for (int xs=0; xs<=layer.size()-2; xs++) {

				
				COORD c1 = layer.get(xs);
				COORD c2 = layer.get(xs+1);
				
				
				corner000 = c1.bottomFront;
				corner010 = c1.topFront;
				corner001 = c1.bottomBack;
				corner011 = c1.topBack;
				
				// meine rechte seite = linke seite des naechsten blocks
				corner100 = c2.bottomFront;
				corner110 = c2.topFront;
				corner101 = c2.bottomBack;
				corner111 = c2.topBack;
				
				Color c = new Color(c1.topFront.y, c1.topFront.y, 0, 0.7f);
				
				if (c1.topFront.y == 0.5f && c1.topBack.y == 0.5f && c2.topFront.y == 0.5f && c2.topBack.y == 0.5f) {
					c = Color.GREEN;
				} else {
					c = Color.YELLOW;
				}
				

//				if (i==0 && b) {
//					c1.topFront.y += 0.2f;
//					c2.topBack.y -= 0.2f;
////					c2.topBack.y += 0.2f;
//				}
				
				Model m1 = finialize(c);
				models.add(m1);
			}
		}
		return models;
	}
	
	static Vector3 corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner111;
	
	public static Model finialize(Color color) {
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = .9f;

		ModelBuilder builder = new ModelBuilder();
		builder.begin();

		java.awt.Color c = new java.awt.Color(color.r, color.g, color.b);
		Material material = createDiffuseMaterialFromImage(c);

		// GL_TRIANGLES
		MeshPartBuilder mpb = builder.part("box", GL20.GL_TRIANGLES, 
				VertexAttributes.Usage.Position
				| VertexAttributes.Usage.ColorPacked
				| VertexAttributes.Usage.Normal 
				| VertexAttributes.Usage.TextureCoordinates, 
				material);
		
		
		Vector3 tmpV1 = new Vector3();
		Vector3 tmpV2 = new Vector3();

		mpb.ensureVertices(24);
		mpb.ensureRectangleIndices(6);

		float normalm = 0.5f;

		Vector3 nor = tmpV1.set(corner000).lerp(corner110, normalm).sub(tmpV2.set(corner001).lerp(corner111, normalm)).nor();
		mpb.rect(corner000, corner010, corner110, corner100, nor);
		
		mpb.rect(corner011, corner001, corner101, corner111, nor.scl(-1));
		nor = tmpV1.set(corner000).lerp(corner101, normalm).sub(tmpV2.set(corner010).lerp(corner111, normalm)).nor();
		
		mpb.rect(corner001, corner000, corner100, corner101, nor);

		mpb.rect(corner010, corner011, corner111, corner110, nor.scl(-1));
		nor = tmpV1.set(corner000).lerp(corner011, normalm).sub(tmpV2.set(corner100).lerp(corner111, normalm)).nor();

		mpb.rect(corner001, corner011, corner010, corner000, nor);

		mpb.rect(corner100, corner110, corner111, corner101, nor.scl(-1));

		return builder.end();
	}
	
	public static Texture createTexture(java.awt.Color c) {
		BufferedImage bufferedImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = bufferedImage.createGraphics();
	    
	    g2d.setColor(java.awt.Color.BLACK);
	    g2d.fillRect(0, 0, 80, 80);
	    
	    g2d.setColor(c);
	    g2d.fillRect(1, 1, 78, 78);
//	    g2d.fillRect(0, 0, 80, 80);
	    
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
	public static Material createDiffuseMaterialFromImage(java.awt.Color c) {
		BlendingAttribute blendingAttribute = new BlendingAttribute();
		blendingAttribute.opacity = 1.0f;
		
		return new Material(TextureAttribute.createDiffuse(createTexture(c)), blendingAttribute);
	}
}
