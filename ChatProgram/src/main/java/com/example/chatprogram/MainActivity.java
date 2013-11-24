package com.example.chatprogram;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.ListView;
import android.widget.TextView;

//import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
//import java.util.ArrayList;

public class MainActivity extends Activity
{
    //private ListView mList;
    //private ArrayList<String> arrayList;
    //private MyCustomAdapter mAdapter;
    //private TCPClient mTcpClient;
    //public BufferedReader br;
    EditText editText;
    String username;


    MulticastSocket socket;
    WifiManager.MulticastLock multicastLock;

    class SocketListener implements Runnable
    {
        String str;

        public void run()
        {

            Log.d("SocketListner", "SocketListener Thread running");

            try
            {


                InetAddress sessAddr = InetAddress.getByName(TCPClient.SERVERIP);
                socket = new MulticastSocket(TCPClient.SERVERPORT);
                socket.joinGroup(sessAddr);

                while (true)
                {
                    final TextView t = (TextView)findViewById(R.id.textview);

                    DatagramPacket recpacket;
                    byte[] recbuf = new byte[256];

                    recpacket = new DatagramPacket (recbuf, recbuf.length);
                    socket.receive (recpacket);
                    System.out.println ("Received packet");
                    String s = new String (recpacket.getData());

                    CharSequence cs = t.getText ();
                    str = cs + "\r\n" +  s;

                    //arrayList.add("s: " + s);

                    Log.d("main", "s: " + s);

                    t.post(new Runnable()
                    {
                        public void run()
                        {
                            int scrollAmount = t.getLayout().getLineTop(t.getLineCount()) - t.getHeight();

                            // if there is no need to scroll, scrollAmount will be <=0
                            if (scrollAmount > 0)
                                t.scrollTo(0, scrollAmount + 42);
                            else
                                t.scrollTo(0, 0);

                            t.setMovementMethod(new ScrollingMovementMethod());

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

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Username");
        alert.setMessage("Please enter a username");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                username = input.getText().toString();
            }
        });

        /*
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        */

        alert.show();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //arrayList = new ArrayList<String>();

        editText = (EditText) findViewById(R.id.editText);
        Button send = (Button)findViewById(R.id.send_button);
 /*
        //relate the listView from java to the one created in xml
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mList.setAdapter(mAdapter);
*/
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();


        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editText.getText().toString() != null) {
                    String s = editText.getText().toString();

                    //DatagramSocket socket;
                    new messageSender().execute(s);

                }
                editText.setText("");
            }
        });

        // RESETS THE EDITTEXT BOX
        //editText.setText("");

        Thread t = new Thread (new SocketListener ());
        t.start();

        }

    /*
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
            //arrayList.add(values[0]);

            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            //mAdapter.notifyDataSetChanged();
        }

    }*/


    /*
    SEND FUNCTION
     */


    public class messageSender extends AsyncTask<String, String, String>
    {
        protected String doInBackground(String... message)
        {

            try
            {

                byte[] buf;
                if(editText.getText().toString() != null)
                {


                    String s = username + ": ";

                    s += editText.getText().toString();

                    if(s.length() != (username.length() + 2))
                    {
                        System.out.println("s.length()" + s.length());
                        System.out.println("string: " + s);
                        System.out.println("username length(): " + username.length());
                        buf = s.getBytes ();
                        InetAddress address = InetAddress.getByName (TCPClient.SERVERIP);

                        DatagramPacket packet = new DatagramPacket (buf, buf.length, address, TCPClient.SERVERPORT);

                        System.out.println ("About to send message");

                        socket.send (packet);

                        //packet.setLength(0);
                        System.out.println ("Sent message");
                    }

                    //s = "";
                }


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