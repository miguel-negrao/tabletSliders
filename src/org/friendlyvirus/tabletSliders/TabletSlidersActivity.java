package org.friendlyvirus.tabletSliders;

import android.os.*;
import android.util.Log;
import controlP5.*;
import processing.core.*;
// Import oscP5 and netP5 libraries
import oscP5.*;
import netP5.*;

import java.util.HashMap;
import java.util.Map;


public class TabletSlidersActivity extends PApplet {

    //String ip = "192.168.1.102";
    String ip = "192.168.0.14";
    NetAddress scClient = new NetAddress( ip, 57120);

    //create one worker thread
    OSCThread mThread = new OSCThread();

    ControlP5 cp5;

    int numSlidersPerRow = 10;
    int numRows = 2;
    float horizontalSpacingRatio = 0.4f;
    float verticalSpacingRatio = 0.2f;

    Map<Slider,int[]> sliderMap = new HashMap<Slider, int[]>();

    int tabHeight = 30;

    String[] pages = { "Page 2", "Page 3" };
    String[] allpages = { "default", "Page 2", "Page 3" };

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



        int [][] sliderSetup = { {3,12}, {2, 12}, {2,12} };

        for( int k = 0; k < allpages.length; k++ ) {

            int numRows = sliderSetup[k][0];
            int numSlidersPerRow = sliderSetup[k][1];

            int horizontalSize = (int) (((float) width) * (1 - horizontalSpacingRatio) / numSlidersPerRow);
            int verticalSize = (int) (((float) height) * (1 - verticalSpacingRatio) / numRows);
            float horizontalSpacing = width * horizontalSpacingRatio / (numSlidersPerRow + 1);
            float verticalSpacing = height * verticalSpacingRatio / (numRows + 1);

            for (int i = 0; i < numRows; i++)
                for (int j = 0; j < numSlidersPerRow; j++) {
                    Slider sl = cp5.addSlider(k+" - "+i+" - "+j)
                            .setPosition(
                                    horizontalSpacing + (j * (horizontalSpacing + horizontalSize)),
                                    verticalSpacing + (i * (verticalSpacing + verticalSize))
                            )
                            .setSize(horizontalSize, verticalSize)
                            .setRange(0.0f, 1.0f)
                            .setId(count-1)
                            .moveTo( allpages[k] );
                    count++;
                    int[] x = { k, i, j };
                    sliderMap.put( sl, x );
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
        Slider sl = (Slider) theEvent.getController();
        int[] slCode = sliderMap.get(sl);
        println("got a control event from controller with code "+slCode[0]+" "+slCode[1]+" "+slCode[2]+" label: " +
                ""+theEvent.getController().getLabel());
        OscMessage oscMsg = new OscMessage("/slider");
        for( int i : slCode ) {
            oscMsg.add(i);
        }
        oscMsg.add(value);

        Message msg = Message.obtain();
        msg.obj =  oscMsg;
        mThread.mHandler.sendMessage(msg);

    }


}
