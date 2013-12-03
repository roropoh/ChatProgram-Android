package com.example.chatprogram;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class MainActivity extends Activity
{
    public static final String SERVERIP = "224.1.1.1"; //your computer IP address
    public static final int SERVERPORT = 4444;
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


                InetAddress sessAddr = InetAddress.getByName(SERVERIP);
                socket = new MulticastSocket(SERVERPORT);
                socket.joinGroup(sessAddr);

                while (true)
                {
                    final TextView t = (TextView)findViewById(R.id.textview);

                    DatagramPacket recpacket;
                    byte[] recbuf = new byte[256];

                    recpacket = new DatagramPacket (recbuf, recbuf.length);
                    socket.receive (recpacket);
                    //System.out.println ("Received packet");
                    String s = new String (recpacket.getData());
                    s = s.trim();
                    CharSequence cs = t.getText ();
                    str = cs + "\r\n" +  s;

                    //arrayList.add("s: " + s);

                    Log.d("main", "s: " + s);

                    t.post(new Runnable()
                    {
                        public void run()
                        {

                            int scrollAmount = t.getLayout().getLineTop(t.getLineCount()) - t.getHeight() ;

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

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+

        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                String temp;

                    temp = possibleEmail.substring(0, possibleEmail.indexOf("@"));

                input.setText(temp, TextView.BufferType.EDITABLE);

            }
        }

        username = input.getText().toString();



        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                username = input.getText().toString();
            }
        });

        alert.show();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editText = (EditText) findViewById(R.id.editText);
        Button send = (Button)findViewById(R.id.send_button);

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

        Thread t = new Thread (new SocketListener ());
        t.start();

        }

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
                        //System.out.println("s.length()" + s.length());
                        //System.out.println("string: " + s);
                        //System.out.println("username length(): " + username.length());
                        buf = s.getBytes ();
                        InetAddress address = InetAddress.getByName (SERVERIP);

                        DatagramPacket packet = new DatagramPacket (buf, buf.length, address, SERVERPORT);

                        //System.out.println ("About to send message");

                        socket.send (packet);
                        //System.out.println ("Sent message");
                    }
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