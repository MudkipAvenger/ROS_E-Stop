package com.example.levi.ros_e_stop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import org.ros.RosCore;
import org.ros.RosRun;
import org.ros.address.InetAddressFactory;
import org.ros.android.MasterChooser;
import org.ros.android.RosActivity;
import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.topic.TopicDescription;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.NodeIdentifier;
import org.ros.internal.node.server.master.MasterRegistrationManagerImpl;
import org.ros.internal.node.server.master.MasterServer;
import org.ros.internal.node.topic.PublisherDeclaration;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.topic.TopicDeclaration;
import org.ros.internal.node.topic.TopicIdentifier;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;

import geometry_msgs.Twist;
import std_msgs.Bool;
import std_msgs.String;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jaredrummler.android.device.DeviceName;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends RosActivity implements RotationGestureDetector.OnRotationGestureListener
{

    private ImageButton redButton;  //button
    private EStopNode eStopNode;    //ROS node
    private Vibrator vibrate;       //to make phone vibrate

    private java.lang.String ipAddress;     //IP address of phone to be appended on ROS node name
                                            //this will allow multiple instances of this app to be run at the same name

    private float rotationReturnPoint = 0;  //snap back point for the button to stick to if rotated beyond boundary
    private float startAngle;               //the angle of the button's rotation when rotation gesture is detected

    private boolean pressed = false;        //pressed state of the button

    private RotationGestureDetector mRotationDetector;  //detects a rotate gesture on the button

    private AdView mAdView;


    //sets up views and events
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DEBUG", "Function is onCreate");

        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);    //init vibrator
        redButton = (ImageButton) findViewById(R.id.ib);                    //get button view

        mRotationDetector = new RotationGestureDetector(this, redButton);   //init rotation detector

        redButton.setScaleX(1.15f);     //make button bigger
        redButton.setScaleY(1.15f);
        redButton.setOnTouchListener(new View.OnTouchListener() {   //set up buttons touch listener
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mRotationDetector.onTouchEvent(event);      //send touch events to rotate detector

                if(event.getAction() == MotionEvent.ACTION_DOWN && !pressed)    //if pressing down and button not pushed
                {

                    final int x = (int) event.getX();
                    final int y = (int) event.getY();

                    //now map the coords we got to the
                    //bitmap (because of scaling)
                    ImageView imageView = ((ImageButton)v);
                    Bitmap bitmap =((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    int pixel = bitmap.getPixel(x,y);

                    //now check alpha for transparency
                    int alpha = Color.alpha(pixel);
                    if(alpha != 0) {
                        pressed = true;         //set flag
                        vibrate.vibrate(300);   //vibrate
                        redButton.animate().scaleX(1).scaleY(1).setDuration(200).start();   //make button smaller to look pressed down, animate                 }
                    }

                }
                return false;
            }
        });

        Log.d("DEBUG", "Function is onCreate finish");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }

    public MainActivity() { super("e_stop_node", "e_stop_node"); }   //for ROS

    protected void init(NodeMainExecutor nodeMainExecutor)  //called when ROS master starts node
    {
        Log.d("DEBUG", "Function is init");
        ipAddress = InetAddressFactory.newNonLoopback().getHostAddress();   //get IP address
        ipAddress = ipAddress.replace(".", "_");        //replace '.'s with '_'s for a valid node name

        eStopNode = new EStopNode();        //create ros node object

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());    //set up node configuration
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(eStopNode, nodeConfiguration);     //start the node
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d("DEBUG", "Function is running");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                //vibrate.vibrate(200);
                Intent i = new Intent(this,ChangeVelOutputTopicActivity.class);
                startActivityForResult(i, 1);
                return true;
            case R.id.help:
                vibrate.vibrate(1000);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {

        }
    }

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector)    //for rotation detector
    {
            float angle = rotationDetector.getAngle();  //get delta angle
            float finalAngle = startAngle + (angle);    //add delta angle to our start angle

            //rotation return point is the point the button will be rotated to if they pass the boundary
            //for example if they try to rotate to -10 degrees, it will stay at 0

            if(finalAngle >= 0 && finalAngle <= 45)     //if in the first half of the rotation, set return to the beginning
                rotationReturnPoint = 0;
            else if(finalAngle <= 90 && finalAngle > 45)    //if in second half of the rotation, set return to end
                rotationReturnPoint = 90;

            if(!(finalAngle >= 0 && finalAngle < 90))       //if final angle is out of bounds, go back to the return point
            {
                finalAngle = rotationReturnPoint;
            }
            redButton.setRotation(finalAngle);              //set the rotation of the button
    }


    public void OnRotationStop(RotationGestureDetector rotationDetector)    //for rotate detector, called when rotation stops
    {
        if(redButton.getRotation() != 0)    //if button is not already at 0, animate and rotate back to 0
        {
            float finalRotation = redButton.getRotation();  //get the current button rotation
            redButton.animate().rotation(0).setDuration(300).start();   //start the animation rotating back to 0
            redButton.setRotation(0);                                   //make sure rotation is set to 0
            rotationReturnPoint = 0;                                    //reset return point
            if(pressed && (finalRotation > 80))     //if button is pressed and is rotated far enough, release button
            {
                redButton.animate().scaleX(1.15f).scaleY(1.15f).setDuration(300).start();   //make button back to normal size
                pressed = false;                            //reset flag
                vibrate.vibrate(new long[]{0, 200, 200, 200}, -1);  //vibrate two times
            }
        }
    }

    public void OnRotationBegin(RotationGestureDetector rotationGestureDetector)    //for rotation detector, called when rotation starts
    {
        startAngle = redButton.getRotation();   //store starting angle for calculating final angle
    }


    //ROS Node Inner Class

    private class EStopNode extends AbstractNodeMain
    {
        private Publisher<geometry_msgs.Twist> velPublisher;
        private Publisher<std_msgs.Bool> statusPublisher;
        private Publisher<std_msgs.String> modelPublisher;


        private Timer statusUpdateTimer;
        private TimerTask statusUpdateTask;
        int statusTime = 1000;

        private boolean publish = true;
        private java.lang.String outputVelTopicName = "/e_stop/cmd_vel";

        //private ConnectedNode myConnectedNode;

        @Override
        public GraphName getDefaultNodeName() {return GraphName.of("/E_Stop_Node_" + ipAddress);}

        @Override
        public void onStart(final ConnectedNode connectedNode)
        {
            velPublisher = connectedNode.newPublisher(GraphName.of(outputVelTopicName), geometry_msgs.Twist._TYPE);
            statusPublisher = connectedNode.newPublisher(GraphName.of("/e_stop/status"), Bool._TYPE);
            modelPublisher = connectedNode.newPublisher(GraphName.of("/" + getDefaultNodeName().toString() + "/model"), String._TYPE);

            modelPublisher.setLatchMode(true);
            String modelMsg = modelPublisher.newMessage();
            modelMsg.setData(DeviceName.getDeviceName());
            modelPublisher.publish(modelMsg);

            statusUpdateTask = new TimerTask() {
                @Override
                public void run() {
                    updateStatus();
                }
            };

            statusUpdateTimer = new Timer();

            statusUpdateTimer.schedule(statusUpdateTask, 0, statusTime);

            final CancellableLoop loop = new CancellableLoop() {
                @Override
                protected void loop() throws InterruptedException {

                    if(pressed && publish)
                    {
                        geometry_msgs.Twist twist = velPublisher.newMessage();
                        twist.getLinear().setX(0);
                        twist.getLinear().setY(0);
                        twist.getLinear().setZ(0);
                        twist.getAngular().setX(0);
                        twist.getAngular().setY(0);
                        twist.getAngular().setZ(0);
                        velPublisher.publish(twist);
                    }

                    Thread.sleep(10);
                }
            };
            connectedNode.executeCancellableLoop(loop);
        }

        @Override
        public void onShutdown(Node node) { Log.d("DEBUG", "in node shutdown");}

        @Override
        public void onShutdownComplete(Node node) {}

        private void updateStatus()
        {

            std_msgs.Bool msg = statusPublisher.newMessage();
            msg.setData(true);
            statusPublisher.publish(msg);
        }

    }
}
