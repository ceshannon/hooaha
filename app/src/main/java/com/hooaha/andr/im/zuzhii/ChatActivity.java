package com.hooaha.andr.im.zuzhii;

import android.app.Notification;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hooaha.andr.im.zuzhii.entity.Ip;
import com.hooaha.andr.im.zuzhii.entity.User;
import com.hooaha.andr.im.zuzhii.entity.XmppChat;
import com.hooaha.andr.im.zuzhii.entity.XmppFriend;
import com.hooaha.andr.im.zuzhii.util.SaveUserUtil;
import com.hooaha.andr.im.zuzhii.util.TimeUtil;
import com.hooaha.andr.im.zuzhii.util.ToastUtil;
import com.hooaha.andr.im.zuzhii.view.CircleImageView;
import com.hooaha.andr.im.zuzhii.view.MsgListView;
import com.hooaha.andr.im.zuzhii.xmpp.XmppContentProvider;
import com.hooaha.andr.im.zuzhii.xmpp.XmppFriendMessageProvider;
import com.hooaha.andr.im.zuzhii.xmpp.XmppReceiver;
import com.hooaha.andr.im.zuzhii.xmpp.XmppService;
import com.hooaha.andr.im.zuzhii.xmpp.ZuzhiiXMPP;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconTextView;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.squareup.picasso.Picasso;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/1/19.
 */
public class ChatActivity extends FragmentActivity implements View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener, MsgListView.IXListViewListener {


    @Bind(R.id.msg_listView)
    MsgListView msgListView;
    @Bind(R.id.iv_icon)
    ImageView ivIcon;
    @Bind(R.id.editEmojicon)
    EmojiconEditText editEmojicon;
    @Bind(R.id.emojicons)
    FrameLayout emojicons;
    @Bind(R.id.aboutme_layout_back)
    LinearLayout aboutmeLayoutBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.btn_send)
    TextView btnSend;

    boolean bool = false;
    List<XmppChat> list;
    public static XmppFriend xf;
    ChatAdapter adapter;
    private ChatManager chatManager;
    private Chat chat;
    User user;
    XmppReceiver receiver;
    public static ChatActivity ca;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ca = this;
        ButterKnife.bind(this);
        initialView();
        initialData();
        connection_chat();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 初始化控件
     */
    public void initialView() {
        receiver = new XmppReceiver(uc);
        registerReceiver(receiver, new IntentFilter("xmpp_receiver"));
        user = SaveUserUtil.loadAccount(this);
        msgListView.setXListViewListener(this);
        ivIcon.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        aboutmeLayoutBack.setOnClickListener(this);
        btnSend.setEnabled(false);
        editEmojicon.setOnClickListener(this);
        xf = (XmppFriend) getIntent().getSerializableExtra("xmpp_friend");
        tvTitle.setText(xf.getUser().getNickname());
        setEmojiconFragment(false);
        msgListView.setPullLoadEnable(false);
        msgListView.setPullRefreshEnable(false);
        editEmojicon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1) {
                    btnSend.setEnabled(false);
                    btnSend.setBackgroundResource(R.drawable.chat_text_send_nomal);

                } else {
                    btnSend.setEnabled(true);
                    btnSend.setBackgroundResource(R.drawable.chat_text_send_foc);
                }
            }
        });
    }


    /**
     * 初始化数据
     */
    public void initialData() {

        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                list = new ArrayList<>();
                if(cursor != null) {
                    while (cursor.moveToNext()) {
                        String main = cursor.getString(cursor.getColumnIndex("main"));
                        String user = cursor.getString(cursor.getColumnIndex("user"));
                        String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                        String icon = cursor.getString(cursor.getColumnIndex("icon"));
                        int type = cursor.getInt(cursor.getColumnIndex("type"));
                        String content = cursor.getString(cursor.getColumnIndex("content"));
                        String sex = cursor.getString(cursor.getColumnIndex("sex"));
                        String too = cursor.getString(cursor.getColumnIndex("too"));
                        String times = cursor.getString(cursor.getColumnIndex("time"));
                        long time = Long.parseLong(times);
                        int viewType = cursor.getInt(cursor.getColumnIndex("viewtype"));
                        XmppChat xm = new XmppChat(main, user, nickname, icon, type, content, sex, too, viewType, time);
                        Log.i("chat》》》》》》》》》》》", too + "\n" + user.toLowerCase());
                        list.add(xm);
                    }
                }
                adapter = new ChatAdapter();
                msgListView.setAdapter(adapter);
                msgListView.setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, XmppFriendMessageProvider.CONTENT_CHATS_URI, null,
                "main=?", new String[]{user.getUser() + xf.getUser().getUser()}, null);
    }

    /**
     * 建立聊天
     */
    private void connection_chat() {
        chatManager = ChatManager.getInstanceFor(ZuzhiiXMPP.getInstance().getCon());
//        chatManager = ZuzhiiXMPP.getInstance().getCon().getChatManager();
        chat = chatManager.createChat(xf.getUser().getUser().toLowerCase() + "@" + ZuzhiiXMPP.getInstance().getCon().getServiceName(), null);
        Log.i("chat》》》》》》》》》》》", xf.getUser().getUser().toLowerCase() + "@" + ZuzhiiXMPP.getInstance().getCon().getServiceName());

    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message m) {


        }


    };

    XmppReceiver.updateActivity uc = new XmppReceiver.updateActivity() {

        @Override
        public void update(String type) {

        }


        @Override
        public void update(XmppChat xc) {
            if (list != null) {

                list.add(xc);
                adapter.setData(list);


            }

        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_icon:
                if (!bool) {
                    hintKbTwo();
                    emojicons.setVisibility(View.VISIBLE);
                    bool = true;
                } else {
                    emojicons.setVisibility(View.GONE);
                    bool = false;
                }

                break;
            case R.id.btn_send:
                if (ZuzhiiXMPP.getInstance().getCon().isConnected() == false) {
                    ToastUtil.show(ChatActivity.this, "已断开,正在重连中....");
                    return;
                }

                try {
                    chat.sendMessage(editEmojicon.getText().toString());
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                ContentValues values = new ContentValues();
                values.put("content", editEmojicon.getText().toString());
                values.put("time", TimeUtil.getDate());
                values.put("result", 0);
                XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "main=? and type=?", new String[]{user.getUser() + xf.getUser().getUser(), "chat"});
                btnSend.setEnabled(false);
                XmppChat xc = new XmppChat(user.getUser() + xf.getUser().getUser(), user.getUser(), user.getNickname(), user.getIcon(), 1, editEmojicon.getText().toString(), user.getSex(), xf.getUser().getUser(), 1, new Date().getTime());
                list.add(xc);
                XmppFriendMessageProvider.add_message(xc);
                adapter.setData(list);
                editEmojicon.setText("");

                break;
            case R.id.editEmojicon:
                if (bool) {
                    emojicons.setVisibility(View.GONE);
                    bool = false;
                }
                break;
            case R.id.aboutme_layout_back:
                ca = null;
                if (MainActivity.main != null) {
                    if (MainActivity.main.message_fragment != null) {
                        ContentValues values1 = new ContentValues();
                        values1.put("result", 0);
                        XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values1, "main=? and type=?", new String[]{user.getUser() + xf.getUser().getUser(), "chat"});
                        MainActivity.main.message_fragment.initialData();
                    }
                }
                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }


    class ChatAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public ChatAdapter() {
            this.mInflater = LayoutInflater.from(ChatActivity.this);
        }

        public void setData(List<XmppChat> lists) {
            list = lists;
            adapter.notifyDataSetChanged();
            msgListView.setSelection(lists.size());
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {

            return list.get(position).getViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String url_icon = Ip.ip + "/YfriendService/DoGetIcon?name=";
            ViewHolder viewHolder;
            int type = list.get(position).getType();
            if (convertView == null || convertView.getTag(R.mipmap.ic_launcher + type) == null) {

                if (type == 2) {
                    convertView = mInflater.inflate(R.layout.chat_intput, parent, false);
                } else {
                    convertView = mInflater.inflate(R.layout.chat_output, parent, false);
                }
                viewHolder = buildHolder(convertView);
                convertView.setTag(R.mipmap.ic_launcher + type, viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag(R.mipmap.ic_launcher
                        + type);
            }
            viewHolder.tv_content.setText(list.get(position).getContent());
            viewHolder.tv_name.setText(list.get(position).getNickname());
            if (list.get(position).getTime() == 0) {
                viewHolder.tv_time.setText("");
            } else {
                viewHolder.tv_time.setText(TimeUtil.getChatTime(list.get(position).getTime()));
            }

            if (list.get(position).getIcon() == null || list.get(position).getIcon().equals("")) {
                if (list.get(position).getSex() == null || list.get(position).getSex().equals("男")) {
                    viewHolder.iv_icon.setImageResource(R.mipmap.me_icon_man);
                } else {
                    viewHolder.iv_icon.setImageResource(R.mipmap.me_icon_woman);
                }
            } else {
                if (list.get(position).getIcon().substring(0, 4).equals("http")) {
                    Picasso.with(ChatActivity.this).load(list.get(position).getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(viewHolder.iv_icon);
                } else {
                    Picasso.with(ChatActivity.this).load(url_icon + list.get(position).getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(viewHolder.iv_icon);
                }
            }


            return convertView;
        }
    }

    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
        holder.tv_content = (EmojiconTextView) convertView.findViewById(R.id.tv_content);
        holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
        holder.iv_icon = (CircleImageView) convertView.findViewById(R.id.iv_icon);
        return holder;
    }

    private static class ViewHolder {
        EmojiconTextView tv_content;
        TextView tv_name;
        TextView tv_time;
        CircleImageView iv_icon;

    }


    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(editEmojicon);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(editEmojicon, emojicon);
    }


    @Override
    public void onBackPressed() {

        if (bool) {
            emojicons.setVisibility(View.GONE);
            bool = false;

        } else {
            ca = null;
            if (MainActivity.main != null) {
                if (MainActivity.main.message_fragment != null) {
                    ContentValues values = new ContentValues();
                    values.put("result", 0);
                    XmppService.resolver.update(XmppContentProvider.CONTENT_MESSAGES_URI, values, "main=? and type=?", new String[]{user.getUser() + xf.getUser().getUser(), "chat"});
                    MainActivity.main.message_fragment.initialData();
                }
            }
            finish();

        }
    }

    //此方法只是关闭软键盘
    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


}
