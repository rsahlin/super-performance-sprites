package com.super2k.tiledspriteengine;

import java.io.IOException;
import java.util.Random;

import com.graphicsengine.io.GSONGraphicsEngineFactory;
import com.graphicsengine.scene.GraphicsEngineNodeType;
import com.graphicsengine.scene.SceneSerializerFactory;
import com.graphicsengine.sprite.Sprite;
import com.graphicsengine.sprite.SpriteNodeFactory;
import com.graphicsengine.spritemesh.SpriteMeshNode;
import com.nucleus.CoreApp;
import com.nucleus.CoreApp.ClientApplication;
import com.nucleus.actor.J2SELogicProcessor;
import com.nucleus.camera.ViewFrustum;
import com.nucleus.geometry.Mesh;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.ViewNode;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, ClientApplication {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    /**
     * Must be set from scenetree
     */
    private int spritecount;
    // TODO How to find these values from the scene
    private static float ORTHO_LEFT = -0.5f;
    private static float ORTHO_TOP = 0.5f;
    private final static float ZOOM_FACTOR = 1f;

    Window window;
    CoreApp coreApp;
    RootNode root;
    NucleusRenderer renderer;
    private int spriteFrames;
    private SpriteMeshNode spriteNode;
    private ViewNode viewNode;
    private int currentSprite = 0;
    private Random random = new Random();

    public final static int DEFAULT_MAX_X = 1;
    public static float[] worldLimit;
    // Ugly fix just to enable stopping sprites
    public static boolean process = false;

    public SuperSprites() {
        super();
    }

    @Override
    public void init(CoreApp coreApp) {
        this.coreApp = coreApp;
        renderer = coreApp.getRenderer();
        coreApp.getInputProcessor().addMMIListener(this);
        coreApp.getRenderer().addContextListener(this);
        SpriteNodeFactory.setActorResolver(new SuperSpriteResolver());
    }

    @Override
    public void inputEvent(MMIPointerEvent event) {

        switch (event.getAction()) {
        case INACTIVE:
            break;
        case MOVE:
        case ACTIVE:
            float[] pos = event.getPointerData().getCurrentPosition();
            releaseSprite(pos, event.getPointerData().getDelta(1));
            process = true;
            break;
        case ZOOM:
            Vector2D zoom = event.getZoom();
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[VecMath.X]))
                    * ZOOM_FACTOR;
            updateNodeScale(z);
            break;
        default:

        }
    }

    private void updateNodeScale(float zoom) {
        if (viewNode != null) {
            viewNode.getView().scale(zoom);
            float[] scale = viewNode.getView().getScale();
            System.out.println("scale: " + scale[VecMath.X]);
            worldLimit[0] = (ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[1] = (ORTHO_TOP) / scale[VecMath.Y];
            worldLimit[2] = (DEFAULT_MAX_X + ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[3] = (-ORTHO_TOP) / scale[VecMath.Y];
        }

    }

    private void releaseSprite(float[] pos, float[] delta) {
        if (!fetchSprites()) {
            return;
        }
        float[] scale = root.getViewNode(Layer.SCENE).getView().getScale();
        float x = (pos[0] / scale[VecMath.X]);
        float y = (pos[1] / scale[VecMath.Y]);
        Sprite s = spriteNode.getSprites()[currentSprite];
        // s.setPosition(x, y, 0);
        s.floatData[Sprite.X_POS] = x;
        s.floatData[Sprite.Y_POS] = y;
        s.setMoveVector(0, 0, 0);
        s.floatData[BounceSprite.ELASTICITY] = 1f - (random.nextFloat() / 5);
        if (delta != null) {
            s.moveVector.setNormalized((delta[0] * 30) / scale[0], 0);
        } else {
            s.moveVector.setNormalized(0, 0);
        }
        s.floatData[BounceSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
        s.setFrame(random.nextInt(spriteFrames));
        s.setScale(0.8f + random.nextFloat() * 0.5f, 0.8f + random.nextFloat() * 0.5f);

        currentSprite++;
        if (currentSprite > spritecount - 1) {
            currentSprite = 0;
        }
    }

    /**
     * Fetches the framecount and number of sprites
     */
    private boolean fetchSprites() {
        if (spriteNode != null) {
            return true;
        }
        spriteNode = getSpriteNode(root);
        if (spriteNode != null) {
            viewNode = root.getViewNode(Layer.SCENE);
            updateNodeScale(0);
            Mesh mesh = spriteNode.getMeshById(spriteNode.getMeshRef());
            // TODO A method to query the mesh how many frames it supports?
            // Maybe a way to fetch the texture from the resources?
            TiledTexture2D tiledTexture = (TiledTexture2D) mesh.getTexture(Texture2D.TEXTURE_0);
            spriteFrames = tiledTexture.getTileWidth() * tiledTexture.getTileHeight();
            spritecount = spriteNode.getCount();
            System.out.println("Spritecount: " + spritecount + ", Spriteframes: " + spriteFrames);
            initSprites(spriteNode);
            return true;
        }
        return false;
    }

    private void initSprites(SpriteMeshNode sprites) {
        int frame = 0;
        int width = (int) Math.sqrt(spritecount);
        int x = 0;
        float xpos = worldLimit[0];
        float ypos = worldLimit[1];
        float delta = (worldLimit[2] - worldLimit[0]) / width;
        Sprite s;
        for (int i = 0; i < spritecount; i++) {
            s = sprites.getSprites()[i];
            s.floatData[Sprite.X_POS] = xpos;
            s.floatData[Sprite.Y_POS] = ypos;
            s.setFrame(frame++);
            s.floatData[Sprite.ROTATION] = (float) i / 360;
            if (frame > spriteFrames) {
                frame = 0;
            }
            xpos += delta;
            x++;
            if (x > width) {
                x = 0;
                xpos = worldLimit[0];
                ypos -= delta;
            }
        }
    }

    private SpriteMeshNode getSpriteNode(RootNode root) {
        if (root == null) {
            return null;
        }
        return (SpriteMeshNode) root.getScene().getNodeByType(GraphicsEngineNodeType.spriteMeshNode.name());
    }

    @Override
    public void contextCreated(int width, int height) {
        window = Window.getInstance();
        worldLimit = new float[] { ORTHO_LEFT, ORTHO_TOP, DEFAULT_MAX_X + ORTHO_LEFT,
                -ORTHO_TOP };
        // Todo - should have a method indicating that context is lost
        spriteNode = null;
        try {
            SceneSerializer sf = SceneSerializerFactory.getSerializer(GSONGraphicsEngineFactory.class.getName(),
                    renderer, GSONGraphicsEngineFactory.getNodeFactory(),
                    GSONGraphicsEngineFactory.getMeshFactory());
            root = sf.importScene("assets/scene.json");
            coreApp.setRootNode(root);
            coreApp.addPointerInput(root);
            /**
             * TODO - this should be handled in a more generic way.
             */
            Node scene = root.getScene();
            ViewFrustum vf = scene.getViewFrustum();
            if (vf != null) {
                float[] values = vf.getValues();
                float w = Math.abs(values[ViewFrustum.LEFT_INDEX] - values[ViewFrustum.RIGHT_INDEX]);
                float h = Math.abs(values[ViewFrustum.TOP_INDEX] - values[ViewFrustum.BOTTOM_INDEX]);
                // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
                coreApp.getInputProcessor().setPointerTransform(w / width, h / -height, values[ViewFrustum.LEFT_INDEX],
                        values[ViewFrustum.TOP_INDEX]);
                ORTHO_LEFT = values[ViewFrustum.LEFT_INDEX];
                ORTHO_TOP = values[ViewFrustum.TOP_INDEX];
            } else {
                // If y is going down then reverse y so that 0 is at bottom which is the same as OpenGL
                coreApp.getInputProcessor().setPointerTransform((float) 1 / width, (float) 1 / -height, -0.5f, 0.5f);
            }

            renderer.getRenderSettings().setClearFunction(GLES20.GL_COLOR_BUFFER_BIT);
            renderer.getRenderSettings().setDepthFunc(GLES20.GL_NONE);
            renderer.getRenderSettings().setCullFace(GLES20.GL_NONE);

            TiledTexture2D tex = (TiledTexture2D) root.getResources().getTexture2D("sprite-texture");
            spriteFrames = tex.getTileWidth() * tex.getTileHeight();
            coreApp.setLogicProcessor(new J2SELogicProcessor());
            fetchSprites();
            try {
                sf.exportScene(System.out, root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
