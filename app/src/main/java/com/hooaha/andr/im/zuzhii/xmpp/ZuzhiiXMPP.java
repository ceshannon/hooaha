package com.hooaha.andr.im.zuzhii.xmpp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.hooaha.andr.im.zuzhii.R;
import com.hooaha.andr.im.zuzhii.entity.XmppUser;
import com.hooaha.andr.im.zuzhii.util.SharedPreferencesUtil;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Created by haoliu on 2016/11/30.
 */
public class ZuzhiiXMPP {
    private static final String DOMAIN = "zuzhii.com";
    private static final String HOST = "123.57.67.3";
    private static final int PORT = 6322;
    private String userName ="";
    private String passWord = "";
    AbstractXMPPConnection connection = null;
    ChatManager chatmanager ;
    Chat newChat;
    XMPPConnectionListener connectionListener = new XMPPConnectionListener();
    private boolean connected;
    private boolean isToasted;
    private boolean chat_created;
    private boolean loggedin;
    private static ZuzhiiXMPP instance;

    public static ZuzhiiXMPP getInstance() {

        if (null == instance)
            instance = new ZuzhiiXMPP();
        return instance;
    }
    //Initialize
    private ZuzhiiXMPP() {
        Log.i("XMPP", "Initializing!");

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
//        configBuilder.setUsernameAndPassword(userName, passWord);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setResource("Android");
        configBuilder.setServiceName(DOMAIN);
        configBuilder.setHost(HOST);
        configBuilder.setPort(PORT);
        //configBuilder.setDebuggerEnabled(true);
        connection = new XMPPTCPConnection(configBuilder.build());
        try {
            connection.connect();
            connected = true;
        } catch (XMPPException e) {
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addConnectionListener(connectionListener);

    }

    public XMPPConnection getCon() {
        return connection;
    }

    // Disconnect Function
    public void disconnectConnection(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connectConnection()
    {
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                // Create a connection
                try {
                    connection.connect();
//                    login();
                    connected = true;

                } catch (XMPPException e) {
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        connectionThread.execute();
    }


    public void sendMsg() {
        if (connection.isConnected()== true) {
            // Assume we've created an XMPPConnection name "connection"._
            chatmanager = ChatManager.getInstanceFor(connection);
            newChat = chatmanager.createChat("concurer@zuzhii.com");

            try {
                newChat.sendMessage("Howdy!");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public void login(String userId,String pwd) {

        this.userName = userId;
        this.passWord = pwd;
        Log.i("LOGIN", "Yey! We're connected to the Xmpp server...........!");
        try {
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
            SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
            SASLAuthentication.blacklistSASLMechanism("X-OAUTH2");
            connection.login(userName, passWord);
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }

    }

    /**
     * 是否与服务器连接上
     *
     * @return
     */
    public boolean isConnection() {
        if (connection != null) {
            return (connection.isConnected() && connection.isAuthenticated());
        }
        return false;
    }

    /**
     * 获取所有分组
     *
     * @param
     * @return
     */
    public List<RosterGroup> getGroups() {
        Roster roster = Roster.getInstanceFor(connection);

        if (!roster.isLoaded()) {
            try {
                roster.reloadAndWait();
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        Collection <RosterGroupEntry> entries = roster.getEntries();
//
//        for (RosterEntry entry : entries)
//            System.out.println("Here: " + entry);
        List<RosterGroup> list = new ArrayList<RosterGroup>();
        list.addAll(roster.getGroups());
        return list;
    }

    /**
     * 获取某一个分组的成员
     *
     * @param
     * @param groupName
     * @return
     */
    public List<RosterEntry> getEntrysByGroup(String groupName) {

        Roster roster = Roster.getInstanceFor(connection);
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        RosterGroup group = roster.getGroup(groupName);
        Collection<RosterEntry> rosterEntiry = group.getEntries();
        Iterator<RosterEntry> iter = rosterEntiry.iterator();
        while (iter.hasNext()) {
            RosterEntry entry = iter.next();
            Log.i("xmpptool", entry.getUser() + "\t" + entry.getName() + entry.getType().toString());
            if (entry.getType().toString().equals("both")) {
                list.add(entry);
            }

        }
        return list;

    }

    /**
     * 添加好友
     *
     * @param
     * @param userName
     * @param name
     * @param groupName 是否有分组
     * @return
     */
    public boolean addUser(String userName, String name, String groupName) {
        Roster roster = Roster.getInstanceFor(connection);
        try {
            roster.createEntry(userName, name, null == groupName ? null
                    : new String[]{groupName});
            return true;
        } catch (XMPPException e) {
//            Log.e(tag, Log.getStackTraceString(e));
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除好友
     *
     * @param userName
     * @return
     */
    public boolean removeUser(String userName) {
        Roster roster = Roster.getInstanceFor(connection);
        try {
            RosterEntry entry = roster.getEntry(userName);
            if (null != entry) {
                roster.removeEntry(entry);
            }
            return true;
        } catch (XMPPException e) {
//            SLog.e(tag, Log.getStackTraceString(e));
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 添加到分组
     *
     * @param
     * @param userName
     * @param groupName
     */
    public void addUserToGroup(String userName, String groupName) {
        Roster roster = Roster.getInstanceFor(connection);
        RosterGroup group = roster.getGroup(groupName);
        if (null == group) {
            group = roster.createGroup(groupName);
        }
        RosterEntry entry = roster.getEntry(userName);
        if (entry != null) {
            try {
                group.addEntry(entry);
            } catch (XMPPException e) {
//                SLog.e(tag, Log.getStackTraceString(e));
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 查找用户
     *
     * @param
     * @param userName
     * @return
     */
    public List<XmppUser> searchUsers(String userName) {
        List<XmppUser> list = new ArrayList<XmppUser>();
        UserSearchManager userSearchManager = new UserSearchManager(connection);
        try {
            Form searchForm = userSearchManager.getSearchForm("vjud."
                    + connection.getServiceName());

            Form answerForm = searchForm.createAnswerForm();
            List<FormField> ff = answerForm.getFields();
            answerForm.setAnswer("nick", userName);
//            answerForm.setAnswer("Name", true);
//            answerForm.setAnswer("Jabber ID", true);
//            answerForm.setAnswer("search", userName);
            ReportedData data = userSearchManager.getSearchResults(answerForm,
                    "vjud." + connection.getServiceName());
            Iterator<ReportedData.Row> rows = data.getRows().iterator();
            while (rows.hasNext()) {
                XmppUser user = new XmppUser(null, null);
                ReportedData.Row row = rows.next();
                user.setUserName(row.getValues("jid").get(0).toString());
                user.setName(row.getValues("nick").get(0).toString());
                list.add(user);
            }
        } catch (XMPPException e) {
//            Log.e(tag, Log.getStackTraceString(e));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 判断是否是好友
     *
     * @param
     * @param user
     * @return
     */
    public boolean isFriendly(String user) {
        Roster roster = Roster.getInstanceFor(connection);
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        list.addAll(roster.getEntries());
        for (int i = 0; i < list.size(); i++) {
            Log.i("xmppttttttttt", list.get(i).getUser().toUpperCase() + "\t" + user);
            if (list.get(i).getUser().contains(user.toLowerCase())) {
                if (list.get(i).getType().toString().equals("both")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;

    }

    //Connection Listener to check connection state
    public class XMPPConnectionListener implements ConnectionListener {
        public void connectionClosed(){

        }

        public void connectionClosedOnError(Exception var1){}

        public void reconnectingIn(int var1){}

        public void reconnectionSuccessful(){}

        public void reconnectionFailed(Exception var1){}
//        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("xmpp", "Connected!");

        }

        public void authenticated(XMPPConnection connection, boolean resumed){
            Log.d("xmpp", "Authenticated!");
        }


//
//        @Override
//        public void connectionClosed() {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//
//
//                    }
//                });
//            Log.d("xmpp", "ConnectionCLosed!");
//            connected = false;
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void connectionClosedOnError(Exception arg0) {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                    }
//                });
//            Log.d("xmpp", "ConnectionClosedOn Error!");
//            connected = false;
//
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectingIn(int arg0) {
//
//            Log.d("xmpp", "Reconnectingin " + arg0);
//
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionFailed(Exception arg0) {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//
//
//                    }
//                });
//            Log.d("xmpp", "ReconnectionFailed!");
//            connected = false;
//
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void reconnectionSuccessful() {
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//
//
//
//                    }
//                });
//            Log.d("xmpp", "ReconnectionSuccessful");
//            connected = true;
//
//            chat_created = false;
//            loggedin = false;
//        }
//
//        @Override
//        public void authenticated(XMPPConnection arg0, boolean arg1) {
//            Log.d("xmpp", "Authenticated!");
//            loggedin = true;
//
//            chat_created = false;
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//            }).start();
//            if (isToasted)
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//
//
//
//                    }
//                });
//        }
    }

    /**
     * 设置状态
     *
     * @param state
     */
    public void setPresence(int state) {
        Presence presence;
        switch (state) {
            //0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线
            case 0:
                presence = new Presence(Presence.Type.available);
                try {
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
//                Log.e(tag, "设置在线");
                break;
            case 1:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.chat);
                try {
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
//                SLog.e(tag, "Q我吧");
//                SLog.e(tag, presence.toXML());
                break;
            case 2:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.dnd);
                try {
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
//                SLog.e(tag, "忙碌");
//                SLog.e(tag, presence.toXML());
                break;
            case 3:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.xa);
                try {
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
//                SLog.e(tag, "勿扰");
//                SLog.e(tag, presence.toXML());
                break;
            case 4:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.away);
                try {
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
//                SLog.e(tag, "离开");
//                SLog.e(tag, presence.toXML());
                break;
            case 5:
//                Roster roster = con.getRoster();
//                Collection<RosterEntry> entries = roster.getEntries();
//                for (RosterEntry entity : entries) {
//                    presence = new Presence(Presence.Type.unavailable);
//                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);
//                    presence.setFrom(con.getUser());
//                    presence.setTo(entity.getUser());
//                    con.sendPacket(presence);
//                    SLog.e(tag, presence.toXML());
//                }
//                SLog.e(tag, "告知其他用户-隐身");

                break;
//            case 6:
//                presence = new Presence(Presence.Type.unavailable);
//                con.sendPacket(presence);
//                SLog.e(tag, "离线");
//                SLog.e(tag, presence.toXML());
//                break;
//            default:
//                break;
        }
    }

    public void setPresence(ImageView iv, ImageView iv_me, Context context, String name) {

        int status = SharedPreferencesUtil.getInt(context, "status", name + "status");
        switch (status) {
            //0.在线 1.Q我吧 2.忙碌 3.勿扰 4.离开 5.隐身 6.离线
            case 0:
                iv.setImageResource(R.mipmap.status_online);
                iv_me.setImageResource(R.mipmap.status_online);
                break;
            case 1:
                iv.setImageResource(R.mipmap.status_qme);
                iv_me.setImageResource(R.mipmap.status_qme);
                break;
            case 2:
                iv.setImageResource(R.mipmap.status_busy);
                iv_me.setImageResource(R.mipmap.status_busy);
                break;
            case 3:
                iv.setImageResource(R.mipmap.status_shield);
                iv_me.setImageResource(R.mipmap.status_shield);
                break;
            case 4:
                iv.setImageResource(R.mipmap.status_leave);
                iv_me.setImageResource(R.mipmap.status_leave);
                break;
            case 5:
                iv.setImageResource(R.mipmap.status_invisible);
                iv_me.setImageResource(R.mipmap.status_invisible);
                break;

        }
    }

}
