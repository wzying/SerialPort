package com.ying.serialport;


import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    int i=0;
    EditText mReception;
    FileOutputStream mOutputStream;
    FileInputStream mInputStream;
    SerialPort sp;//要实例化了才能使用，要不就不正常
    Thread thread;
    int size;
    byte[] buffer = new byte[64];
    TextView textsize;

    public Handler mHandler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 1:
                    if (mReception != null)
                        mReception.append(new String(buffer, 0, size));
                    textsize.setText("size : " + size);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReception = (EditText)findViewById(R.id.editText);
        textsize = (TextView)findViewById(R.id.textsize);

        thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                while (true) {

                    try {
                        size = mInputStream.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);

//                    try {
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });


    }

    public void console(View view) {
        try {
            sp=new SerialPort(new File("/dev/ttySAC3"),115200);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        mOutputStream=(FileOutputStream) sp.getOutputStream();
        mInputStream=(FileInputStream) sp.getInputStream();

        /* 启动接收线程（阻塞接收） */
        thread.start();

        Toast.makeText(getApplicationContext(), "open",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickSend(View view) {

        try {
            mOutputStream.write(new String("send").getBytes());
            mOutputStream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }


        Toast.makeText(getApplicationContext(), "send",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickReceive(View view) {
        int size;

        try {
            byte[] buffer = new byte[64];
            if (mInputStream == null) return;
            size = mInputStream.read(buffer);
            if (size > 0) {
                onDataReceived(buffer, size);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mReception != null) {
                    mReception.append(new String(buffer, 0, size));
                    textsize.setText("size : " + size);
                }
            }
        });
    }

    public void onClickClose(View view) {
        sp.close();//用实例化的对象的方法。因为线程一直在阻塞读，关闭后会有问题。
    }
}
