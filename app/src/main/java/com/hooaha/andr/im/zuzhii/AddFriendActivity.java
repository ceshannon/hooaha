package com.hooaha.andr.im.zuzhii;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hooaha.andr.im.zuzhii.entity.Ip;
import com.hooaha.andr.im.zuzhii.entity.User;
import com.hooaha.andr.im.zuzhii.entity.XmppUser;
import com.hooaha.andr.im.zuzhii.util.ToastUtil;
import com.hooaha.andr.im.zuzhii.view.CircleImageView;
import com.hooaha.andr.im.zuzhii.xmpp.ZuzhiiXMPP;
import com.squareup.picasso.Picasso;


import org.jivesoftware.smack.XMPPConnection;

import java.util.List;

/**
 * Created by Administrator on 2016/1/8.
 */
public class AddFriendActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private EditText searchView;
    private ListView listView;
    private Button btn;
    private List<XmppUser> users;
    private LinearLayout ll_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        initialView();
    }

    @Override
    public void initialView() {

        ll_back = (LinearLayout) findViewById(R.id.addfriend_layout_back);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchView = (EditText) findViewById(R.id.editText1);

        listView = (ListView) findViewById(R.id.listView1);
        btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ZuzhiiXMPP.getInstance().isConnection() == false) {
                    ToastUtil.show(AddFriendActivity.this, "已断开,正在重连中....");
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        users = ZuzhiiXMPP.getInstance().searchUsers(
                                searchView.getText().toString());
                        Message m = new Message();
                        m.what = 1;
                        h.sendMessage(m);
                    }
                }.start();


            }
        });
        listView.setOnItemClickListener(this);
    }

    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (users != null) {

                    listView.setAdapter(new MyAdapter());
                }

            }
        }
    };

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View view, ViewGroup arg2) {
            if (null == view) {
                view = LayoutInflater.from(AddFriendActivity.this).inflate(
                        R.layout.friend_message_item, null);
            }

//            User user = new Gson().fromJson(users.get(pos).getName(), User.class);
            XmppUser xu = users.get(pos);
            User user = new User();
            user.setNickname(xu.getName());
            user.setIcon("");
            user.setSex("");
            TextView textView_name = (TextView) view.findViewById(R.id.textView_name);
            TextView textView_city = (TextView) view.findViewById(R.id.textView_city);
            CircleImageView imageview_icon = (CircleImageView) view.findViewById(R.id.imageview_icon);
            textView_name.setText(user.getNickname());
            if (user.getCity() == null || user.getCity().equals("")) {
                user.setCity("未知星球");
            }
            textView_city.setText(user.getCity());
            if (user.getIcon().equals("")) {
                if (user.getSex().equals("男")) {
                    imageview_icon.setImageResource(R.mipmap.me_icon_man);
                } else {
                    imageview_icon.setImageResource(R.mipmap.me_icon_woman);
                }
            } else {
                if (user.getIcon().substring(0, 4).equals("http")) {
                    Picasso.with(AddFriendActivity.this).load(user.getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(imageview_icon);
                } else {
                    Picasso.with(AddFriendActivity.this).load(Ip.ip_icon + user.getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(imageview_icon);
                }
            }
            return view;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        if (ZuzhiiXMPP.getInstance().isConnection() == false) {
            ToastUtil.show(AddFriendActivity.this, "已断开,正在重连中....");
            return;
        }

        Intent intent = new Intent(AddFriendActivity.this, ShowMessageActivity.class);
        intent.putExtra("user", users.get(pos).getName());
        startActivity(intent);

//        if (XmppTool.getInstance().addUser(
//                users.get(pos).getUserName() + "@" + con.getServiceName(),
//                users.get(pos).getName(), null)) {
//            XmppTool.getInstance().addUserToGroup(
//                    users.get(pos).getUserName() + "@" + con.getServiceName(),
//                    "我的好友");
//            Log.i("search",
//                    "申请添加" + users.get(pos).getUserName() + "@"
//                            + con.getServiceName() + "为好友");
//            finish();
//        } else {
//
//            Log.i("search", "添加失败");
//        }

    }
}
