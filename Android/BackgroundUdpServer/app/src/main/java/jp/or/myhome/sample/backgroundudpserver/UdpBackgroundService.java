package jp.or.myhome.sample.backgroundudpserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpBackgroundService extends Service {
    public static final String TAG = MainActivity.TAG;

    static final String CHANNEL_ID = "default";
    static final String CHANNEL_ID2 = "notification";
    static final String NOTIFICATION_TITLE = "Udpバックグラウンドサービス";
    static final String NOTIFICATION_CONTENT = "Udpパケット待ち受け中";
    static final String NOTIFICATION_MESSAGE = "通知メッセージ";
    static final int NOTIFICATION_ID = 1;
    private DatagramSocket udpReceive;
    boolean force_stop = true;
    NotificationManager notificationManager;
    static final int UDP_BUFFER_SIZE = 10 * 1024;

    public UdpBackgroundService() {
        Log.d(TAG, "MyService constructor");
    }

    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate called");

        Context context = getApplicationContext();
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_TITLE , NotificationManager.IMPORTANCE_DEFAULT);
        // 通知音を消さないと毎回通知音が出てしまう
        // この辺りの設定はcleanにしてから変更
        channel.setSound(null,null);
        channel.enableVibration(false);
        notificationManager.createNotificationChannel(channel);

        NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID2, NOTIFICATION_MESSAGE , NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel2);
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");

        int udpPort = intent.getIntExtra("udpPort", MainActivity.DEFAULT_UDP_PORT);
        Log.d(TAG, "udpPort: " + udpPort);
        try{
            if( udpReceive != null ) {
                udpReceive.close();
                udpReceive = null;
            }
            udpReceive = new DatagramSocket(udpPort);
        }catch(Exception ex){
            Log.d(TAG, ex.getMessage());
        }

        force_stop = false;
        Thread thread = new Thread(new UdpReceiveThread());
        thread.start();

        Intent notifyIntent = new Intent(this, MainActivity.class);
//        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(NOTIFICATION_TITLE)
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentText(NOTIFICATION_CONTENT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");

        force_stop = true;
        if( udpReceive != null ) {
            udpReceive.close();
            udpReceive = null;
        }
    }

    private class UdpReceiveThread implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "UdpReceiveThread run");

            try {
                byte[] buff = new byte[UDP_BUFFER_SIZE];
                while(!force_stop) {
                    Arrays.fill(buff, (byte)0);
                    DatagramPacket packet = new DatagramPacket(buff, buff.length);
                    udpReceive.setSoTimeout(0);
                    udpReceive.receive(packet);
                    Log.d(TAG, "received from " + packet.getAddress());

                    try {
                        JSONObject json = new JSONObject(new String(buff));
                        String type = "";
                        try{
                            type = json.getString("type");
                        }catch(Exception ex){
                            Log.d(TAG, ex.getMessage());
                        }
                        switch(type){
                            case "toast":{
                                Handler mainHandler = new Handler(getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String message = json.getString("message");
                                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                break;
                            }
                            case "notification":{
                                String title = json.getString("title");
                                String message = json.getString("message");

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID2);
                                builder.setContentTitle(title);
                                builder.setContentText(message);
                                builder.setSmallIcon(android.R.drawable.ic_popup_reminder);
                                builder.setAutoCancel(true);
                                notificationManager.notify(0, builder.build());
                                break;
                            }
                        }
                    }catch(Exception ex2){
                        Log.d(TAG, ex2.getMessage());
                    }
                }
            } catch (Exception ex) {
                Log.d(TAG, ex.getMessage());
            } finally {
                Log.d(TAG, "UdpReceiveThread end");
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");

        return null;
    }
}
