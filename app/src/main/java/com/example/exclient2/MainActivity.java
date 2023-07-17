package com.example.exclient2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    // 백그라운드 스레드와 기본 UI 스레드 간의 통신을 허용
    InetAddress serverAddr;
    Socket socket;
    // 서버와의 연결을 설정하는 데 사용
    private BufferedReader receiveReader;
    // 소켓에서 데이터를 읽는 데 사용
    private PrintWriter sendWriter;
    // 데이터를 서버로 전송하는 데 사용
    private String ip = "localhost";
    private int port = 8000;
    //private TextView textView;
    TextView textView;
    // 사용자의 ID를 저장
    String UserID;
    //private Button chatbutton;
    Button chatbutton;
    //private TextView chatView;
    TextView chatView;
    // 대화 메시지를 표시하는 데 사용
    //private EditText message;
    EditText message;
    //private String sendmsg;
    String sendmsg;
    // 전송할 메시지를 저장
    //private String read;
    String read;
    // 수신된 메시지를 저장
    @Override
    protected void onStop() {
    // onStop은 앱이 종료될 때 실행되는 메소드로 sendWriter과 socket을 닫아준다.
        super.onStop();
        try {
            if (sendWriter != null) {
                sendWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } @Override
    protected void onCreate(Bundle savedInstanceState) {
    // onCreate는 onStop과 반대로 앱이 실행될 때 실행되는 메소드
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    // main.xml 파일에 정의된 레이아웃으로 설정
        mHandler = new Handler();

        textView = (TextView) findViewById(R.id.UserID);
    // main.xml에서 R.id.UserID로 정의된 TextView를 찾아옵니다. 이 뷰는 textView 변수에 할당
        chatView = (TextView) findViewById(R.id.chatView);
    // main.xml에서 R.id.chatView로 정의된 TextView를 찾아옵니다. 이 뷰는 chatView 변수에 할당
        message = (EditText) findViewById(R.id.message);
    // main.xml에서 R.id.message로 정의된 EditText를 찾아옵니다. 이 뷰는 message 변수에 할당
        chatbutton = (Button) findViewById(R.id.chatbutton);

        Intent intent = getIntent();
    // 액티비티 간에 데이터를 전달하는 Intent 객체를 생성

        UserID = intent.getStringExtra("username");
    // 앞서 생성한 Intent 객체에서 "username"이라는 이름으로 저장된 문자열을 가져와서 UserID 변수에 저장
        textView.setText(UserID);
    // textView라는 TextView 객체에 UserID 값을 설정하여 화면에 표시


        new Thread() {
            // 소켓 통신을 위한 스레드를 생성하고 실행합니다.
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(ip);
                    //InetAddress.getByName(ip)는 지정된 IP 주소를 가진 호스트의 인터넷 주소를 반환
                    socket = new Socket("220.69.207.113", port);
                    //Socket(serverAddr, port)는 서버에 접속하기 위한 소켓을 생성
                    sendWriter = new PrintWriter(socket.getOutputStream(), true);
                    //socket.getOutputStream()으로 소켓의 출력 스트림을 얻어서 PrintWriter에 연결
                    // 즉, 서버로 데이터를 전송하기 위한 PrintWriter 객체 생성
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //BufferedReader를 사용하여 소켓으로부터 받은 메시지를 읽음.
                    // 즉, 서버에서 데이터를 받아오기 위한 BufferedReader 객체 생성

                    // Send the nickname to the server
                    sendWriter.println(UserID);
                    sendWriter.flush();

                    while(true){
                        read = input.readLine();
                        //input.readLine()을 사용하여 소켓으로부터 메시지를 읽는다.
                        if(read!=null){
                            mHandler.post(new msgUpdate(read));
                            //mHandler를 사용하여 UI 쓰레드에서 msgUpdate 객체를 실행시켜서 메시지를 화면에 출력
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }} }.start();

    

    // 메세지를 보내는 코드
        chatbutton.setOnClickListener(new View.OnClickListener() {
            // chatbutton 버튼을 클릭했을 때 실행되는 이벤트를 처리
            @Override
            public void onClick(View v) {
                sendmsg = message.getText().toString();
                // sendmsg 변수에 message EditText에서 입력받은 문자열을 저장
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {

                            if (sendWriter != null) { // sendWriter 객체가 null이 아니면

                                sendWriter.println(UserID + " > " + sendmsg);
                                // 서버로 메시지를 전송
                                sendWriter.flush();
                                // 버퍼에 남아있는 내용을 모두 보낸 후, 출력 버퍼를 비움
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                chatView.setText(chatView.getText() + UserID + " > " + sendmsg + "\n");
                message.setText("");
            }
        });

    } class msgUpdate implements Runnable{
        private String msg;
        // 문자열 타입의 msg 선언
        public msgUpdate(String str) {this.msg=str;}
        // msgUpdate 클래스의 매개변수 str로 받은 문자열을 msg 인스턴스 변수에 할당
        @Override
        public void run() {
            chatView.setText(chatView.getText().toString()+msg+"\n");
            // chatView의 setText() 메서드를 호출하여, chatView에 새로운 텍스트를 설정.
        }
    }
}