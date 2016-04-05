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
import com.nucleus.geometry.Mesh;
import com.nucleus.io.SceneSerializer;
import com.nucleus.mmi.MMIEventListener;
import com.nucleus.mmi.MMIPointerEvent;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Layer;
import com.nucleus.renderer.NucleusRenderer.RenderContextListener;
import com.nucleus.renderer.Window;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.RootNode.Scenes;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.TiledTexture2D;
import com.nucleus.vecmath.VecMath;
import com.nucleus.vecmath.Vector2D;

public class SuperSprites implements MMIEventListener, RenderContextListener, ClientApplication {

    protected final static String TILED_SPRITE_RENDERER_TAG = "TiledSpiteRenderer";
    private int SPRITECOUNT = 1200;
    // TODO How to find these values from the scene
    private final static float ORTHO_LEFT = -0.5f;
    private final static float ORTHO_TOP = 0.5f;
    private final static float ZOOM_FACTOR = 0.5f;

    Window window;
    CoreApp coreApp;
    RootNode root;
    NucleusRenderer renderer;
    private SpriteMeshNode spriteNode;
    private int spriteFrames;

    private int currentSprite = 0;
    private Random random = new Random();

    public final static int DEFAULT_MAX_X = 1;
    public final static int DEFAULT_MAX_Y = 1;
    public final static float[] worldLimit = new float[] { ORTHO_LEFT, ORTHO_TOP, DEFAULT_MAX_X + ORTHO_LEFT,
            -ORTHO_TOP };

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
            float[] start = event.getPointerData().getFirstPosition();
            releaseSprite(start, pos);
            break;
        case ZOOM:
            Vector2D zoom = event.getZoom();
            float z = ((zoom.vector[Vector2D.MAGNITUDE] * zoom.vector[VecMath.X]))
                    * ZOOM_FACTOR;
            root.getView().scale(z);
            // root.getNode(Layer.SCENE).getTransform().scale(z);
            float[] scale = root.getView().getScale();
            System.out.println("scale: " + scale[VecMath.X]);
            worldLimit[0] = (ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[1] = (ORTHO_TOP) / scale[VecMath.Y];
            worldLimit[2] = (DEFAULT_MAX_X + ORTHO_LEFT) / scale[VecMath.X];
            worldLimit[3] = (-ORTHO_TOP) / scale[VecMath.Y];
            break;
        default:

        }
    }

    private void releaseSprite(float[] start, float[] pos) {
        if (spriteNode == null) {
            return;
        }
        float[] scale = root.getView().getScale();
        float x = (pos[0] / scale[VecMath.X]);
        float y = (pos[1] / scale[VecMath.Y]);
        Sprite s = spriteNode.getSprites()[currentSprite];
        s.setPosition(x, y, 0);
        s.floatData[Sprite.X_POS] = x;
        s.floatData[Sprite.Y_POS] = y;
        s.setMoveVector(0, 0, 0);
        s.floatData[AFSprite.ELASTICITY] = 0.95f - (random.nextFloat() / 10);
        s.moveVector.setNormalized((pos[0] - start[0]), 0);
        s.floatData[AFSprite.ROTATE_SPEED] = s.moveVector.vector[VecMath.X];
        s.setFrame(random.nextInt(spriteFrames));
        s.setScale(0.8f + random.nextFloat() * 0.5f, 0.8f + random.nextFloat() * 0.5f);

        s.floatData[Sprite.SCALE] = 1 + random.nextFloat();

        currentSprite++;
        if (currentSprite > SPRITECOUNT - 1) {
            currentSprite = 0;
        }
    }

    @Override
    public void contextCreated(int width, int height) {
        window = Window.getInstance();

        if (spriteNode == null) {
            try {
                SceneSerializer sf = SceneSerializerFactory.getSerializer(GSONGraphicsEngineFactory.class.getName(),
                        renderer, GSONGraphicsEngineFactory.getNodeFactory(),
                        GSONGraphicsEngineFactory.getMeshFactory());
                root = sf.importScene("assets/scene.json");
                LayerNode credit = root.getScene(Scenes.credit);
                LayerNode game = root.getScene(Scenes.game);
                Node sprites = game.getNodeByType(GraphicsEngineNodeType.spriteMeshNode.name());
                coreApp.setRootNode(root);
                root.setLayer(Layer.SCENE);

                renderer.getRenderSettings().setClearFunction(GLES20.GL_COLOR_BUFFER_BIT);
                renderer.getRenderSettings().setDepthFunc(GLES20.GL_NONE);
                renderer.getRenderSettings().setCullFace(GLES20.GL_NONE);

                if (sprites != null && sprites instanceof SpriteMeshNode) {
                    spriteNode = (SpriteMeshNode) sprites;
                    Mesh mesh = spriteNode.getMeshById(spriteNode.getMeshRef());
                    // TODO A method to query the mesh how many frames it supports?
                    TiledTexture2D tiledTexture = (TiledTexture2D) mesh.getTexture(Texture2D.TEXTURE_0);
                    spriteFrames = tiledTexture.getTileWidth() * tiledTexture.getTileHeight();
                    SPRITECOUNT = spriteNode.getCount();

                }
                coreApp.setLogicProcessor(new J2SELogicProcessor());
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

        } else {
            System.err.println("NOT IMPLEMENTED");
        }
    }
}
