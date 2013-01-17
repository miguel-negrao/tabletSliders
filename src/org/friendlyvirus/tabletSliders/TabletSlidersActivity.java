package org.friendlyvirus.tabletSliders;

import android.os.*;
import android.util.Log;
import android.view.MotionEvent;
import controlP5.*;
import processing.core.*;
// Import oscP5 and netP5 libraries
import oscP5.*;
import netP5.*;


public class TabletSlidersActivity extends PApplet {

    //String ip = "192.168.1.102";
    String ip = "192.168.1.1";
    NetAddress scClient = new NetAddress( ip, 57120);

    //create one worker thread
    OSCThread mThread = new OSCThread();

    ControlP5 cp5;

    int numSlidersPerRow = 10;
    int numRows = 2;
    float horizontalSpacingRatio = 0.4f;
    float verticalSpacingRatio = 0.2f;

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

        int count = 1;

        int horizontalSize = (int) (((float) width) * (1 - horizontalSpacingRatio) / numSlidersPerRow);
        int verticalSize = (int) (((float) height) * (1 - verticalSpacingRatio) / numRows);
        float horizontalSpacing = width * horizontalSpacingRatio / (numSlidersPerRow + 1);
        float verticalSpacing = height * verticalSpacingRatio / (numRows + 1);

        for (int i = 0; i < numRows; i++)
            for (int j = 0; j < numSlidersPerRow; j++) {
                cp5.addSlider("" + count)
                        .setPosition(
                                horizontalSpacing + (j * (horizontalSpacing + horizontalSize)),
                                verticalSpacing + (i * (verticalSpacing + verticalSize))
                        )
                        .setSize(horizontalSize, verticalSize)
                        .setRange(0.0f, 1.0f)
                        .setId(count-1);
                count++;
            }

    }


    public void draw() {
        background(0);

    }


    public void controlEvent(ControlEvent theEvent) {
        /* events triggered by controllers are automatically forwarded to
           the controlEvent method. by checking the id of a controller one can distinguish
           which of the controllers has been changed.
        */

        println("got a control event from controller with id "+theEvent.getController().getId());
        float value = theEvent.getValue();
        int id = theEvent.getController().getId();
        OscMessage oscMsg = new OscMessage("/slider");
        oscMsg.add(id);
        oscMsg.add(value);

        Message msg = Message.obtain();
        msg.obj =  oscMsg;
        mThread.mHandler.sendMessage(msg);

    }


}
