package com.example.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity1 extends AppCompatActivity {
    private Thread mThread = null;
    private EditText etIP, etPort;
    private TextView tvMessages;
    private EditText etMessage;
    private String SERVER_IP;
    private int SERVER_PORT;

    private BufferedReader reader;
    private Socket socket;
    private String tmp;
    private PrintWriter out;
    private String message;
    private String clientName; // 保存客户端的名字

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        EditText clientname = findViewById(R.id.clientName);
        Button btnLeave = findViewById(R.id.clientleave);
        Button btnConnect = findViewById(R.id.btnConnect);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString());
                clientName = clientname.getText().toString();

                mThread = new Thread(new clientThread());
                mThread.start();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = etMessage.getText().toString();
                if (!message.isEmpty()) {
                    String fullmessage = clientName + ": " + message;
                    new Thread(new SendData(fullmessage)).start();
                }
            }
        });

        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = "已離開";
                if (!message.isEmpty()) {
                    String fullmessage = clientName + ": " + message;
                    new Thread(new SendData(fullmessage)).start();
                }
                finish();
            }

        });

    }

    class clientThread implements Runnable {
        public void run() {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);

                if(socket.isConnected()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           tvMessages.setText("Connected\n");
                            String fullmessage = clientName + ": 已連上";
                            new Thread(new SendData(fullmessage)).start();
                        }
                    });
                    // 取得網路輸入串流
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //取得網路輸出串流
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    while ((tmp = reader.readLine()) != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("收 "
                                        +": " + tmp + "\n");
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class SendData implements Runnable {
        private String smessage;

        SendData(String message) {
            this.smessage = message;
        }
        @Override
        public void run() {
            out.println(smessage);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("發 "
//                            +socket.getLocalAddress().getHostAddress()
                            +": " + smessage + "\n");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }
}