package com.evergrande.ttyusb.ttyusb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {
    private String TAG = "ttyusb";
    private Button button;
    private TextView tv;
    private SerialPortUtil serialPortUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btn);
        tv = (TextView) findViewById(R.id.tv);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        serialPortUtil = new SerialPortUtil();
        serialPortUtil.openSerialPort();

        //注册EventBus
        EventBus.getDefault().register(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*点击打开串口开始测试*/
                serialPortUtil.getSerialPort();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        serialPortUtil.closeSerialPort();
        serialPortUtil.closeThread();
    }

    /**
     * 用EventBus进行线程间通信，也可以使用Handler
     * @param string
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String string){
        tv.append(string+'\n');
        if(string.equals(Cmd.TEST_CMD_FINISH)){
            //
        }
    }
}
