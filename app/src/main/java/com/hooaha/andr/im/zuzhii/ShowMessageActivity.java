package com.hooaha.andr.im.zuzhii;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hooaha.andr.im.zuzhii.entity.Ip;
import com.hooaha.andr.im.zuzhii.entity.User;
import com.hooaha.andr.im.zuzhii.entity.XmppFriend;
import com.hooaha.andr.im.zuzhii.entity.XmppUser;
import com.hooaha.andr.im.zuzhii.fragment.UserMessageFragment;
import com.hooaha.andr.im.zuzhii.fragment.UserStatusFragment;
import com.hooaha.andr.im.zuzhii.util.ToastUtil;
import com.hooaha.andr.im.zuzhii.view.CircleImageView;
import com.hooaha.andr.im.zuzhii.xmpp.XmppService;
import com.hooaha.andr.im.zuzhii.xmpp.ZuzhiiXMPP;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;


/**
 * Created by Administrator on 2016/2/3.
 */
public class ShowMessageActivity extends FragmentActivity implements View.OnClickListener {


    @Bind(R.id.layout_back)
    LinearLayout layoutBack;
    @Bind(R.id.iv_icon)
    CircleImageView ivIcon;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.tv_city)
    TextView tvCity;
    @Bind(R.id.layout_tab)
    TabLayout layoutTab;
    @Bind(R.id.viewpager)
    ViewPager viewpager;
    @Bind(R.id.btn_add)
    Button btnAdd;

    String user;
    User users;
    String name;
    UserMessageFragment messgae_fragment;
    UserStatusFragment status_fragment;
    Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        ButterKnife.bind(this);
        user = getIntent().getStringExtra("user");
        getUserMessage();

        layoutBack.setOnClickListener(this);
        btnAdd.setOnClickListener(this);


    }

    /**
     * 获取用户资料
     */
    void getUserMessage() {

        List<XmppUser> list = ZuzhiiXMPP.getInstance().searchUsers(user.toLowerCase());
        XmppUser xu =  list.get(0);
        name = list.get(0).getName();
        users = new User();
        users.setNickname(xu.getName());
        users.setUser(xu.getName());
        users.setIcon("");
        users.setSex("");
        users.setCity("");

        initialView();
        tvName.setText(users.getNickname());
        if (users.getCity().equals("")) {
            tvCity.setText("未知星球");
        } else {
            tvCity.setText(users.getCity());
        }

        if (users.getIcon().equals("")) {
            if (users.getSex().equals("男")) {
                ivIcon.setImageResource(R.mipmap.me_icon_man);
            } else {
                ivIcon.setImageResource(R.mipmap.me_icon_woman);
            }
        } else {
            if (users.getIcon().substring(0, 4).equals("http")) {
                Picasso.with(ShowMessageActivity.this).load(users.getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(ivIcon);
            } else {
                Picasso.with(ShowMessageActivity.this).load(Ip.ip_icon + users.getIcon()).resize(200, 200).placeholder(R.mipmap.qq_addfriend_search_friend).error(R.mipmap.qq_addfriend_search_friend).centerInside().into(ivIcon);
            }
        }
        ivIcon.setOnClickListener(ShowMessageActivity.this);
        if (XmppService.user.equals(user)) {
            btnAdd.setVisibility(View.GONE);
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            if (ZuzhiiXMPP.getInstance().isFriendly(user)) {
                btnAdd.setText("发信息");
            } else {
                btnAdd.setText("加好友");
            }
        }

    }


    private void initialView() {
        FragmentManager manager = this.getSupportFragmentManager();
        if (manager != null) {
            adapter = new Adapter(manager);
            viewpager.setAdapter(adapter);
            layoutTab.setupWithViewPager(viewpager);
            layoutTab.setTabMode(TabLayout.MODE_FIXED);

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_icon:
                if (users != null) {
//                    Intent in = new Intent(this, ShowImageActivity.class);
//                    String path = "";
//                    if (users.getIcon().equals("")) {
//                        path = users.getSex();
//                    } else {
//                        path = users.getIcon();
//                    }
//                    in.putExtra("path", path);
//                    in.putExtra("type", "icon");
//                    startActivity(in);
                }

                break;
            case R.id.layout_back:
                finish();
                break;
            case R.id.btn_add:
                if (btnAdd.getText().equals("发信息")) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("xmpp_friend", new XmppFriend(users));
                    startActivity(intent);
                    finish();

                } else if (btnAdd.getText().equals("加好友")) {
                    //15612610827;江城北望丶;15612610827_20151202_232508.jpg;男

                    Log.i("search>>>>", name + "\t" + users.getUser());
                    if (ZuzhiiXMPP.getInstance().addUser(
                            users.getUser() + "@" + ZuzhiiXMPP.getInstance().getCon().getServiceName(),
                            name, null)) {
                        ZuzhiiXMPP.getInstance().addUserToGroup(
                                users.getUser() + "@" + ZuzhiiXMPP.getInstance().getCon().getServiceName(),
                                "我的好友");
                        Log.i("search",
                                "申请添加" + users.getUser() + "@"
                                        + ZuzhiiXMPP.getInstance().getCon().getServiceName() + "为好友");
                        ToastUtil.show(ShowMessageActivity.this, "请求已发送");
                    } else {
                        ToastUtil.show(ShowMessageActivity.this, "请求发送失败，请稍后再试");
                        Log.i("search", "添加失败");
                    }
                }
                break;
        }

    }

    private class Adapter extends FragmentPagerAdapter {
        String str[] = {"资料", "动态"};

        public Adapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return getFragment(position);
        }

        @Override
        public int getCount() {
            return str.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return str[position];
        }

    }

    private Fragment getFragment(int position) {


        switch (position) {

            case 0:

                if (messgae_fragment == null) {
                    messgae_fragment = new UserMessageFragment();
                    Bundle bd = new Bundle();
                    bd.putSerializable("user", users);
                    messgae_fragment.setArguments(bd);

                }
                return messgae_fragment;

            case 1:

                if (status_fragment == null) {
                    status_fragment = new UserStatusFragment();
                    Bundle bd = new Bundle();
                    bd.putSerializable("user", users);
                    status_fragment.setArguments(bd);

                }
                return status_fragment;


        }

        return null;
    }

}
