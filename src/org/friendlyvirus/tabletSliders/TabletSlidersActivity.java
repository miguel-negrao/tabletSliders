package org.friendlyvirus.tabletSliders;

import android.os.*;
import android.util.Log;
import controlP5.*;
import processing.core.*;
// Import oscP5 and netP5 libraries
import oscP5.*;
import netP5.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TabletSlidersActivity extends PApplet {

    public static final String LOG_TAG = "tabletSliders";

    //OSC SEND
    //String ip = "192.168.1.102";
    //String ip = "192.168.0.3";
    String ip = "192.168.2.17";
    NetAddress scClient = new NetAddress( ip, 57120);

    //OSC receive
    OscP5 oscP5 = new OscP5(this, 12000);

    //create one worker thread
    OSCThread mThread = new OSCThread();

    ControlP5 cp5;

    int numSlidersPerRow = 10;
    int numRows = 2;
    float horizontalSpacingRatio = 0.4f;
    float verticalSpacingRatio = 0.2f;

    Map< Slider, List<Integer> > sliderMap = new HashMap< Slider, List<Integer> >();
    Map< List<Integer>, Slider > inverseSliderMap = new HashMap< List<Integer>, Slider >();

    int tabHeight = 30;

    //sad but doing it the logic way is too hard in java...
    //I wonder how does anywone get anything done with java...
    String[] pages = { "Page 2", "Page 3", "Page 4", "Page 5" };
    String[] allpages = { "default", "Page 2", "Page 3", "Page 4", "Page 5" };

    class OSCThread extends Thread {
        public Handler mHandler;

        @Override
        public void run(){
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    OscMessage m = (OscMessage) msg.obj;
                    OscP5.flush( m, scClient);
                }
            };
            Looper.loop();
        }
    }

    public void setup() {
        size(700, 400);
        noStroke();
        //start worker thread
        mThread.start();

        cp5 = new ControlP5(this);

        int tabWidth = (int) ((float) width) / allpages.length;

        for( String label : pages ) {
            cp5.addTab(label)
                    .setColorBackground(color(0, 160, 100))
                    .setColorLabel(color(255))
                    .setColorActive(color(255, 128, 0))
                    .setWidth(tabWidth)
                    .setHeight(tabHeight)
            ;
        }

        cp5.getTab("default")
                .setLabel("Page 1")
                .setWidth(tabWidth)
                .setHeight(tabHeight)
        ;

        int count = 1;

        //setup 1
        //int [][] sliderSetup = { {3,10}, {2, 10}, {2,10} };
        //setup 2
        int [][] sliderSetup = { {2,9}, {2,9}, {2,9}, {2, 10}, {2,10} };

        for( int k = 0; k < allpages.length; k++ ) {

            int numRows = sliderSetup[k][0];
            int numColumns = sliderSetup[k][1];

            int horizontalSize = (int) (((float) width) * (1 - horizontalSpacingRatio) / numColumns);
            int verticalSize = (int) (((float) height) * (1 - verticalSpacingRatio) / numRows);
            float horizontalSpacing = width * horizontalSpacingRatio / (numColumns + 1);
            float verticalSpacing = height * verticalSpacingRatio / (numRows + 1);

            for (int i = 0; i < numRows; i++)
                for (int j = 0; j < numColumns; j++) {
                    Slider sl = cp5.addSlider(k+" - "+i+" - "+j)
                            .setPosition(
                                    horizontalSpacing + (j * (horizontalSpacing + horizontalSize)),
                                    verticalSpacing + (i * (verticalSpacing + verticalSize))
                            )
                            .setSize(horizontalSize, verticalSize)
                            .setRange(0.0f, 1.0f)
                            .setId(count - 1)
                            .setCaptionLabel("AA:" + k + " - " + i + " - " + j)
                            .moveTo(allpages[k]);

                    List<Integer> key = Arrays.asList(k, i, j);
                    sliderMap.put( sl, key );
                    inverseSliderMap.put( key, sl );
                }
        }

        //for debugging
        /*
        Textarea myTextarea = cp5.addTextarea("txt")
                .setPosition(100, 600)
                .setSize(800, 100)
                .setFont(createFont("", 10))
                .setLineHeight(14)
                .setColor(color(200))
                .setColorBackground(color(0, 100))
                .setColorForeground(color(255, 100));
        ;

        Println console = cp5.addConsole(myTextarea);
        console.play();
        */

    }


    public void draw() {
        background(0);
    }


    public void controlEvent(ControlEvent theEvent) {
        /* events triggered by controllers are automatically forwarded to
           the controlEvent method. by checking the id of a controller one can distinguish
           which of the controllers has been changed.
        */


        float value = theEvent.getValue();
        if( theEvent.isController() ) {
            Slider sl = (Slider) theEvent.getController();
            List<Integer> slCode = sliderMap.get(sl);

            if( slCode != null ) {

                //println("got a control event from controller with code "+slCode[0]+" "+slCode[1]+" "+slCode[2]+" label: " +
                //        ""+theEvent.getController().getLabel());
                OscMessage oscMsg = new OscMessage("/slider");
                for( int i : slCode ) {
                    oscMsg.add(i);
                }
                oscMsg.add(value);

                Message msg = Message.obtain();
                msg.obj =  oscMsg;
                mThread.mHandler.sendMessage(msg);
            } else
                println("don't know this controller...");

        }

    }

    void oscEvent(OscMessage theOscMessage)
    {
        /* print the address pattern and the typetag of the received OscMessage */

        print("### received an osc message.");
        print(" addrpattern: "+theOscMessage.addrPattern());
        println(" typetag: "+theOscMessage.typetag());

        String pattern = theOscMessage.addrPattern();

        if( pattern.equals("/sliderLabel") ) {
                println("got a sliderLabel");
                int page = theOscMessage.get(0).intValue();
                int row = theOscMessage.get(1).intValue();
                int column = theOscMessage.get(2).intValue();
                String label = theOscMessage.get(3).stringValue();
                List<Integer> key = Arrays.asList(page, row, column);
                Slider sl = inverseSliderMap.get(key);
                if( sl != null ) {
                    println("setting label to"+label);
                    sl.setCaptionLabel(label);
                } else
                    println("slider doesn't exits !"+page+"-"+row+"-"+column);


        }
    }
}
