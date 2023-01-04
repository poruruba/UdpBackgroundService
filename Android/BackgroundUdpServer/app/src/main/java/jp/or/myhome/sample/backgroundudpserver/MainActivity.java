package jp.or.myhome.sample.backgroundudpserver;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "LogTag";

    public static final int DEFAULT_UDP_PORT = 1234;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int ipaddr = wifiManager.getConnectionInfo().getIpAddress();
        Log.d(TAG, "ipaddress=" + String.format("%d.%d.%d.%d", ((ipaddr >> 0) & 0x00ff), ((ipaddr >> 8) & 0x00ff), ((ipaddr >> 16) & 0x00ff), ((ipaddr >> 24) & 0x00ff) ) );

        pref = getSharedPreferences("Private", Context.MODE_PRIVATE);

        int udpPort = pref.getInt("udp_port", DEFAULT_UDP_PORT );

        TextView text;
        text = (TextView)findViewById(R.id.txt_config_ipaddress);
        text.setText(String.format("%d.%d.%d.%d", ((ipaddr >> 0) & 0x00ff), ((ipaddr >> 8) & 0x00ff), ((ipaddr >> 16) & 0x00ff), ((ipaddr >> 24) & 0x00ff) ));

        Button btn;
        btn = (Button)findViewById(R.id.btn_service_start);
        btn.setOnClickListener(this);
        btn = (Button)findViewById(R.id.btn_service_stop);
        btn.setOnClickListener(this);

        EditText edit;
        edit = (EditText)findViewById(R.id.edit_config_udpport);
        edit.setText(String.valueOf(udpPort));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_service_start:{
                EditText edit;
                edit = (EditText)findViewById(R.id.edit_config_udpport);
                int udpPort = Integer.parseInt(edit.getText().toString());
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("udp_port", udpPort);
                editor.apply();
                Intent intent;
                intent = new Intent(this, UdpBackgroundService.class);
                intent.putExtra("udpPort", 1234);
                startForegroundService(intent);
                Toast.makeText(this, "待ち受けを開始しました。", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.btn_service_stop:{
                Intent intent = new Intent(this, UdpBackgroundService.class);
                stopService(intent);
                Toast.makeText(this, "待ち受けを停止しました。", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }
}