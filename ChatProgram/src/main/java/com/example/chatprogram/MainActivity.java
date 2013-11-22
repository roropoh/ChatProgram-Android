package com.example.chatprogram;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends Activity
{
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;
    public BufferedReader br;


    MulticastSocket socket;
    WifiManager.MulticastLock multicastLock;

    class SocketListener implements Runnable
    {
        String str;

        public void run()
        {
            DatagramPacket packet;
            byte[] buf = new byte[256];
            Log.d("SocketListner", "SocketListener Thread running");

            try
            {


                InetAddress sessAddr = InetAddress.getByName(TCPClient.SERVERIP);
                socket = new MulticastSocket(TCPClient.SERVERPORT);
                socket.joinGroup(sessAddr);

                while (true)
                {
                    final TextView t = (TextView)findViewById(R.id.textview);



                    packet = new DatagramPacket (buf, buf.length);
                    socket.receive (packet);
                    System.out.println ("Received packet");
                    String s = new String (packet.getData());

                    CharSequence cs = t.getText ();
                    str = cs + "\r\n" +  s;

                    //arrayList.add("s: " + s);

                    Log.d("main", "s: " + s);

                    t.post(new Runnable()
                    {
                        public void run()
                        {
                            t.setText(str);
                        }
                    }
                    );

                }
            }
            catch (IOException e)
            {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }



    @Override
    public void onDestroy()
    {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button send = (Button)findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();



        // connect to the server
        // new connectTask().execute("");
        send.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //final EditText et = (EditText)findViewById(R.id.editText);
                //Editable e = et.getText();
                //String s = e.toString();
                String s = editText.getText().toString();

                //DatagramSocket socket;
                 new messageSender().execute(s);
                /*
                try
                {
                    DatagramSocket socket = new DatagramSocket ();

                    byte[] buf = new byte[256];

                    //String outputLine = s; //txArea.getText ();

                    buf = s.getBytes ();
                    InetAddress address = InetAddress.getByName (TCPClient.SERVERIP);
                    DatagramPacket packet = new DatagramPacket (buf, buf.length, address, 4444);
                    System.out.println ("About to send message");
                    socket.send (packet);
                    System.out.println ("Sent message");
                }
                catch (SocketException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (UnknownHostException e2)
                {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                catch (IOException e3) {
                    // TODO Auto-generated catch block
                    e3.printStackTrace();
                }


            */
            }
        });

        Thread t = new Thread (new SocketListener ());
        t.start();
/*
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
            */

        }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }

    }

    public class messageSender extends AsyncTask<String, String, String>
    {
        protected String doInBackground(String... message)
        {
            try
            {
                //DatagramSocket socket = new DatagramSocket ();

                byte[] buf = new byte[256];

                //String outputLine = s; //txArea.getText ();
                String s = "hello world";
                buf = s.getBytes ();
                InetAddress address = InetAddress.getByName (TCPClient.SERVERIP);

                DatagramPacket packet = new DatagramPacket (buf, buf.length, address, TCPClient.SERVERPORT);
                System.out.println ("About to send message");
                socket.send (packet);
                System.out.println ("Sent message");
            }
            catch (SocketException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (UnknownHostException e2)
            {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            catch (IOException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }




            return message[0];
        }
    }
}