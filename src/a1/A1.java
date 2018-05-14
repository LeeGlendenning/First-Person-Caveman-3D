package a1;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class A1 extends SimpleApplication
        implements ActionListener {

  private BulletAppState bulletAppState;
  private Spatial grog;
  private CharacterControl player;
  private Vector3f walkDirection = new Vector3f();
  private boolean left = false, right = false, up = false, down = false;
  private float rad90 = 1.5708f;

  public static void main(String[] args) {
    A1 app = new A1();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    

    flyCam.setMoveSpeed(100);
    setUpKeys();
    
    // set camera in grog's head and look where he is looking
    cam.setLocation(new Vector3f(0.0f, 31.0f, 10.0f));
    //cam.lookAtDirection(new Vector3f(0.0f, 0.0f, 0.0f), Vector3f.UNIT_Z);
    
    // Define dinosaur skin to be added to dino models
    Material dinoSkin = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    dinoSkin.setTexture("ColorMap", assetManager.loadTexture("Textures/dinosaur/triceratops.jpg"));
    
    float dinoZDist = 40.0f;
    float dinoXDist = 45.0f;
    float dinoYDist = 15.0f;
    
    // Define first dinosaur and place at -x,+z corner of fort
    Spatial dino1 = assetManager.loadModel("Models/triceratops/triceratops.j3o");
    dino1.rotate(-rad90, 2*rad90, 0.0f);
    dino1.setMaterial(dinoSkin);
    dino1.setLocalTranslation(-dinoXDist, dinoYDist, dinoZDist);
    rootNode.attachChild(dino1);
    
    // Define 2nd dinosaur and place at +x,+z corner of fort
    Spatial dino2 = assetManager.loadModel("Models/triceratops/triceratops.j3o");
    dino2.rotate(-rad90, -rad90, 0.0f);
    dino2.setMaterial(dinoSkin);
    dino2.setLocalTranslation(3.0f + dinoXDist, 5.0f + dinoYDist, 3.0f + dinoZDist);
    rootNode.attachChild(dino2);
    
    // Define 3rd dinosaur and place at +x,-z corner of fort
    Spatial dino3 = assetManager.loadModel("Models/triceratops/triceratops.j3o");
    dino3.rotate(-rad90, 0.0f, 0.0f);
    dino3.setMaterial(dinoSkin);
    dino3.setLocalTranslation(dinoXDist, dinoYDist, -dinoZDist);
    rootNode.attachChild(dino3);
    
    // Define 4th dinosaur and place at -x,-z corner of fort
    Spatial dino4 = assetManager.loadModel("Models/triceratops/triceratops.j3o");
    dino4.rotate(-rad90, rad90, 0.0f);
    dino4.setMaterial(dinoSkin);
    dino4.setLocalTranslation(-dinoXDist, dinoYDist, -dinoZDist);
    rootNode.attachChild(dino4);
    
    // Define Grog mesh and place in fort
    float grogScaleFactor = 0.3f;
    grog = assetManager.loadModel("Models/grog/grog5k.obj");
    grog.scale(grogScaleFactor, grogScaleFactor, grogScaleFactor);
    //grog.rotate(0.0f, 2*rad90, 0.0f);
    grog.setLocalTranslation(0.0f, 6.0f, 0.0f);
    rootNode.attachChild(grog);
    
    // Load fort
    Spatial terrain = assetManager.loadModel("Scenes/Fort.j3o");
    terrain.rotate(0.0f, -1.5708f, 0.0f); // rotate fort such that doorway in z direction
    terrain.setLocalTranslation(0.0f, -5.0f, 0.0f);
    rootNode.attachChild(terrain);
    
    // Add light to make the model visible
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(-0.1f, -0.7f, 0.0f));
    rootNode.addLight(sun);

    /* We set up collision detection for the scene by creating a static
    RigidBodyControl with mass zero.*/
    terrain.addControl(new RigidBodyControl(0));

    // We set up collision detection for the player by creating
    // a capsule collision shape and a CharacterControl.
    // The CharacterControl offers extra settings for
    // size, stepheight, jumping, falling, and gravity.
    // We also put the player in its starting position.
    
    //This capsule shape bounds the grog
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 30f, 1);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(30);
    player.setPhysicsLocation(new Vector3f(-10, 10, 10));

    // We attach the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    bulletAppState.getPhysicsSpace().add(terrain);
    bulletAppState.getPhysicsSpace().add(player);

  }
  /** We over-write some navigational key mappings here, so we can
   * add physics-controlled walking and jumping: */
  private void setUpKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
  }

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
  public void onAction(String binding, boolean value, float tpf) {
    if (binding.equals("Left")) {
      if (value) { left = true; } else { left = false; }
    } else if (binding.equals("Right")) {
      if (value) { right = true; } else { right = false; }
    } else if (binding.equals("Up")) {
      if (value) { up = true; } else { up = false; }
    } else if (binding.equals("Down")) {
      if (value) { down = true; } else { down = false; }
    } else if (binding.equals("Jump")) {
      player.jump();
    }
  }

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with grog.
   */
  @Override
  public void simpleUpdate(float tpf) {
    Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
    Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
    walkDirection.set(0, 0, 0);
    if (left)  { walkDirection.addLocal(camLeft); }
    if (right) { walkDirection.addLocal(camLeft.negate()); }
    if (up)    { walkDirection.addLocal(camDir); }
    if (down)  { walkDirection.addLocal(camDir.negate()); }
    player.setWalkDirection(walkDirection);
    cam.setLocation(player.getPhysicsLocation());
    
    grog.setLocalTranslation(player.getPhysicsLocation()); // Make the grog move when the camera moves
    // Rotate grog on y axis when camera rotates on y axis (via user's mouse)
    grog.setLocalRotation(new Quaternion(0.0f, cam.getRotation().getY(), 0.0f, cam.getRotation().getW()));
    // Correct grog's rotation so grog points in same direction as camera.
    // Without this, you look through back of grog's head
    grog.rotate(0f, 2*rad90, 0f);
  }
  
}