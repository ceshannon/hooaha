package com.hooaha.andr.im.zuzhii.xmpp;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.hooaha.andr.im.zuzhii.entity.User;
import com.hooaha.andr.im.zuzhii.entity.XmppChat;
import com.hooaha.andr.im.zuzhii.entity.XmppMessage;
import com.hooaha.andr.im.zuzhii.entity.XmppUser;
import com.hooaha.andr.im.zuzhii.util.SaveUserUtil;
import com.hooaha.andr.im.zuzhii.util.SharedPreferencesUtil;
import com.hooaha.andr.im.zuzhii.util.TimeUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by haoliu on 2016/11/30.
 */
public class XmppService extends Service {

    public static ContentResolver resolver;
    public static HashMap<String, Object> map;

    private XMPPConnection con;
    private String userName;
    private String passWord;
    private ZuzhiiXMPP xmpp;
    String s1[];
    String s2[];
    public static String user;

    public XmppService() {

        xmpp = ZuzhiiXMPP.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        resolver = getContentResolver();
        map = new HashMap<>();
        con = ZuzhiiXMPP.getInstance().getCon();
        user = SaveUserUtil.loadAccount(XmppService.this).getUser();
        Log.i("service", "xxxxxxxxx");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder<XmppService>(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        XMPPConnection con =  xmpp.getCon();
        if (null != con && con.isConnected()) {
            StanzaListener sl = new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) {
                    if (packet instanceof IQ) {
                        IQ iq = (IQ) packet;
                        if (iq.getType().equals(IQ.Type.set)) {

                        }
                    }else if (packet instanceof Message) {
                        Message msg = (Message) packet;
                        Log.i("service", msg.getFrom() + " 说：" + msg.getBody() + " 字符串长度：" + msg.getBody().length());
                        int viewType;
                        if (msg.getBody().length() > 3 && msg.getBody().toString().substring(0, 4).equals("http")) {
                            viewType = 2;
                        } else {
                            viewType = 1;
                        }
                        List<XmppUser> list_xu = ZuzhiiXMPP.getInstance().searchUsers(msg.getFrom().split("@")[0]);
//                        User users = new Gson().fromJson(list_xu.get(0).getName(), User.class);
                        XmppUser xu =  list_xu.get(0);
                        User users = new User();
                        users.setNickname(xu.getName());
                        users.setUser(xu.getName());
                        users.setIcon("");
                        users.setSex("");
                        users.setCity("");
                        Log.i("serviceeeeee", list_xu.get(0).getName());
                        XmppChat xc = new XmppChat(user + users.getUser(), users.getUser(), users.getNickname(), users.getIcon(), 2, msg.getBody().toString(), users.getSex(), user, viewType, new Date().getTime());
                        XmppFriendMessageProvider.add_message(xc);
                        sendBroad("chat", xc);
                    }else if (packet instanceof Presence) {
                        Presence presence = (Presence) packet;
                        s1 = presence.getFrom().toUpperCase().split("@");// 发送方
                        s2 = presence.getTo().toUpperCase().split("@");// 接收方
                        if (presence.getType().equals(Presence.Type.subscribe)) {// 好友申请

                            Log.i("service", s1[0] + "\t好友申请加为好友\t type="
                                    + presence.getType().toString());
                            sendBroad("add");

                        } else if (presence.getType().equals(Presence.Type.subscribed)) {// 同意添加好友

                            Log.i("service", s1[0] + "\t同意添加好友\t type="
                                    + presence.getType().toString());
                            sendBroad("tongyi");

                        } else if (presence.getType().equals(Presence.Type.unsubscribe)) {// 删除好友

                            Log.i("service", s1[0] + "\t 删除好友");


                        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {// 拒绝对放的添加请求

                            Log.i("service", s1[0] + "\t 拒绝添加好友");
                            sendBroad("jujue");

                        } else if (presence.getType().equals(Presence.Type.unavailable)) {// 好友下线
                            Log.i("service", s1[0] + "\t 下线了");
                            sendBroad("status", 6);
                        } else if (presence.getType().equals(Presence.Type.available)) {// 好友上线
                            //0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线
                            if (presence.getMode() == Presence.Mode.chat) {//Q我吧
                                Log.i("service", s1[0] + "\t 的状态改为了 Q我吧");
                                sendBroad("status", 1);
                            } else if (presence.getMode() == Presence.Mode.dnd) {//忙碌
                                Log.i("service", s1[0] + "\t 的状态改为了 忙碌了");
                                sendBroad("status", 2);
                            } else if (presence.getMode() == Presence.Mode.xa) {//忙碌
                                Log.i("service", s1[0] + "\t 的状态改为了 勿扰了");
                                sendBroad("status", 3);
                            } else if (presence.getMode() == Presence.Mode.away) {//离开
                                Log.i("service", s1[0] + "\t 的状态改为了 离开了");
                                sendBroad("status", 4);
                            } else {
                                Log.i("service", s1[0] + "\t 的状态改为了 上线了");
                                sendBroad("status", 0);
                            }


                        }
                    }
                }
            };
            con.addAsyncStanzaListener(sl, null);


        }
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);

    }

    private void sendBroad(String type, XmppChat xc) {
        Intent intent;
        intent = new Intent("xmpp_receiver");
        intent.putExtra("type", type);
        intent.putExtra("chat", xc);
        sendBroadcast(intent);
    }

    private void sendBroad(String type, int status) {
        map.put(s1[0], status);
        Intent intent;
        intent = new Intent("xmpp_receiver");
        intent.putExtra("type", type);
        sendBroadcast(intent);
    }

    private void sendBroad(String type) {
        String str_content = "";
        String str_type = "";
        switch (type) {
            case "add":
                str_content = "请求加为好友";
                str_type = "add";
                break;

            case "tongyi":
                str_content = "同意添加好友";
                str_type = "tongyi";
                break;

            case "jujue":
                str_content = "拒绝添加好友";
                str_type = "jujue";
                break;
        }
        List<XmppUser> list2 = ZuzhiiXMPP.getInstance().searchUsers(s1[0]);
        if (msgDatas(s2[0] + list2.get(0).getUserName(), s1[0], s2[0], str_content, str_type)) {
//            if (pool != null && SharedPreferencesUtil.getBoolean(this, "tishi", "music", true)) {
//                pool.play(1, 1, 1, 0, 0, 1);
//            }
//            if(vibrator!=null&& SharedPreferencesUtil.getBoolean(this, "tishi", "zhendong", true)){
//                vibrator.vibrate(500);
//            }
            Intent intent;
            intent = new Intent("xmpp_receiver");
            intent.putExtra("type", type);
            sendBroadcast(intent);
        }


    }
    public boolean msgDatas(final String main, final String users, final String to, final String content, String type) {

        Cursor cursor = XmppService.resolver.query(XmppContentProvider.CONTENT_MESSAGES_URI, null,
                "main=? and type=?", new String[]{main, type}, null);

        if (!cursor.moveToFirst()) {
            //插入
            List<XmppUser> list1 = ZuzhiiXMPP.getInstance().searchUsers(users);
            Log.i("XmppService_add", list1.get(0).getUserName() + "\n" + list1.get(0).getName());
            XmppMessage xm = new XmppMessage(to,
                    type,
                    new XmppUser(list1.get(0).getUserName(), list1.get(0).getName()),
                    TimeUtil.getDate(),
                    content,
                    1,
                    main
            );
            XmppContentProvider.add_message(xm);
            return true;
        } else {
            //更新
            if (type.equals("add")) {
                int result = cursor.getInt(cursor.getColumnIndex("result"));
                if (result == 0) {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    ContentValues values = new ContentValues();
                    values.put("content", content);
                    values.put("time", TimeUtil.getDate());
                    values.put("result", 1);
                    XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?", new String[]{id + ""});
                    return true;
                } else {
                    return false;
                }

            } else {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                ContentValues values = new ContentValues();
                values.put("content", content);
                values.put("time", TimeUtil.getDate());
                values.put("result", 1);
                XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "id=?", new String[]{id + ""});
                return true;
            }


        }

    }

    @Override
    public void onDestroy() {
        xmpp.disconnectConnection();
        super.onDestroy();
    }
}
