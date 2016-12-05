package com.hooaha.andr.im.zuzhii;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hooaha.andr.im.zuzhii.entity.User;
import com.hooaha.andr.im.zuzhii.entity.XmppChat;
import com.hooaha.andr.im.zuzhii.fragment.FriendFragment;
import com.hooaha.andr.im.zuzhii.fragment.MessageFragment;
import com.hooaha.andr.im.zuzhii.view.CircleImageView;
import com.hooaha.andr.im.zuzhii.xmpp.XmppReceiver;
import com.hooaha.andr.im.zuzhii.xmpp.XmppService;
import com.hooaha.andr.im.zuzhii.xmpp.ZuzhiiXMPP;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    public static MainActivity main;
    @Bind(R.id.main_iv_status)
    ImageView mainIvStatus;
    ImageView iv_me_status;

    private TextView tv_news;
    private TextView tv_luntan;
    private TextView tv_friend;
    private TextView tv_message;
    private LinearLayout ll_to;
    private CircleImageView iv_me;
    private CircleImageView iv_mes;
    private TextView tv_me_name;

    ImageView iviv;
    ImageView iv_addfriend;
    TextView tv_tianqi;
    TextView tv_date;

//    private NewsFragment news_fragment;
//    private LuntanFragment luntan_fragment;
    private FriendFragment friend_fragment;
    public MessageFragment message_fragment;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    Intent intent;
    public User user;
    SweetAlertDialog pDialog;

    //xmpp
    public XmppReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        main = this;
        intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        initialView();
    }

    /**
     * 初始化控件
     */
    private void initialView() {
        receiver = new XmppReceiver(ua);
        registerReceiver(receiver, new IntentFilter("xmpp_receiver"));
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = mNavigationView.inflateHeaderView(R.layout.navigation_header);
        iv_me_status = (ImageView) headerView.findViewById(R.id.me_status);
//        iv_me_status = (ImageView) mNavigationView.findViewById(R.id.me_status);
        ZuzhiiXMPP.getInstance().setPresence(mainIvStatus, iv_me_status, this, user.getUser());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mNavigationView != null) {
//            setupDrawerContent(mNavigationView);
        }

        tv_news = (TextView) findViewById(R.id.tv_news);
        tv_luntan = (TextView) findViewById(R.id.tv_luntan);
        tv_friend = (TextView) findViewById(R.id.tv_friend);
        tv_message = (TextView) findViewById(R.id.tv_message);

        ll_to = (LinearLayout) findViewById(R.id.main_layout_to);
        iv_me = (CircleImageView) findViewById(R.id.main_CircleImageView);
        iv_mes = (CircleImageView) mNavigationView.findViewById(R.id.me_icon);
        iviv = (ImageView) mNavigationView.findViewById(R.id.iviv);
        iv_addfriend = (ImageView) findViewById(R.id.main_imageView_addfriend);

//        iv_mes.setOnClickListener(this);
        iv_addfriend.setOnClickListener(this);

//        tv_news.setOnClickListener(this);
//        tv_luntan.setOnClickListener(this);
        tv_friend.setOnClickListener(this);
        tv_message.setOnClickListener(this);
        ll_to.setOnClickListener(this);

        selection(0);
    }

    /**
     * 点击不同的按钮做出不同的处理
     */
    private void selection(int index) {
        initialImage();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                ft.hide(f);
            }
        }

        Fragment fragment;
        switch (index) {

            case 0:
                iv_addfriend.setVisibility(View.GONE);
                tv_message.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_move_pressed_icon, 0, 0);
                tv_message.setTextColor(tv_message.getResources().getColor(R.color.title));
                fragment = getSupportFragmentManager().findFragmentByTag("message_fragment");
                if (fragment == null) {
                    message_fragment = new MessageFragment();
                    ft.add(R.id.fg_content, message_fragment, "message_fragment");
                } else {
                    ft.show(fragment);
                }
                break;
            case 1:
                iv_addfriend.setVisibility(View.VISIBLE);
                tv_friend.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_me_pressed_icon, 0, 0);
                tv_friend.setTextColor(tv_friend.getResources().getColor(R.color.title));
                fragment = getSupportFragmentManager().findFragmentByTag("friend_fragment");
                if (fragment == null) {
                    friend_fragment = new FriendFragment();
                    ft.add(R.id.fg_content, friend_fragment, "friend_fragment");
                } else {
                    ft.show(fragment);
                }
                break;
//
//            case 2:
//                iv_addfriend.setVisibility(View.GONE);
//                tv_news.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_comprehensive_pressed_icon, 0, 0);
//                tv_news.setTextColor(tv_news.getResources().getColor(R.color.title));
//                fragment = getSupportFragmentManager().findFragmentByTag("news_fragment");
//                if (fragment == null) {
//                    news_fragment = new NewsFragment();
//                    ft.add(R.id.fg_content, news_fragment, "news_fragment");
//                } else {
//                    ft.show(fragment);
//                }
//                break;
//            case 3:
//                iv_addfriend.setVisibility(View.GONE);
//                tv_luntan.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_found_pressed_icon, 0, 0);
//                tv_luntan.setTextColor(tv_luntan.getResources().getColor(R.color.title));
//                fragment = getSupportFragmentManager().findFragmentByTag("luntan_fragment");
//                if (fragment == null) {
//                    luntan_fragment = new LuntanFragment();
//                    ft.add(R.id.fg_content, luntan_fragment, "luntan_fragment");
//                } else {
//                    ft.show(fragment);
//                }
//                break;
        }
        ft.commit();
    }

    /**
     * 初始化图片,文字
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initialImage() {
        tv_news.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.tab_comprehensive_icon, 0, 0);
        tv_news.setTextColor(getResources().getColor(R.color.tab_text_bg));
        tv_luntan.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tab_found_icon, 0, 0);
        tv_luntan.setTextColor(getResources().getColor(R.color.tab_text_bg));
        tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.tab_me_icon, 0, 0);
        tv_friend.setTextColor(getResources().getColor(R.color.tab_text_bg));
        tv_message.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.mipmap.tab_move_icon, 0, 0);
        tv_message.setTextColor(getResources().getColor(R.color.tab_text_bg));
    }

    XmppReceiver.updateActivity ua = new XmppReceiver.updateActivity() {
        @Override
        public void update(String type) {

            switch (type) {


                case "status":
                    if (friend_fragment != null) {
                        friend_fragment.getData();
                    }
                    break;

                case "tongyi":
                    if (message_fragment != null) {
                        message_fragment.initialData();
                    }
//                    if (friend_fragment != null) {
//                        friend_fragment.getData();
//                    }
                    break;
                case "add":
                case "jujue":
                case "chat":
                    if (message_fragment != null) {
                        message_fragment.initialData();
                    }
                    break;
            }
        }


        @Override
        public void update(XmppChat xc) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.tv_news:
//                selection(2);
//                break;
//            case R.id.tv_luntan:
//                selection(3);
//                break;
            case R.id.tv_message:
                selection(0);
                break;
            case R.id.tv_friend:
                selection(1);
                break;
            case R.id.main_imageView_addfriend:
                startActivity(new Intent(MainActivity.this, AddFriendActivity.class));
                break;

            case R.id.main_layout_to:
                mDrawerLayout.openDrawer(Gravity.LEFT);//开启抽屉

                break;
//            case R.id.me_icon:
//
//                showPopupWindow(code);
//                // mDrawerLayout.closeDrawers();//关闭抽屉
//                break;
//            case R.id.third_popupwindow_layout_null:
//                if (pop != null) {
//                    pop.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_layout_nulls:
//                if (pops != null) {
//                    pops.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_layout_nullss:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_textView_quxiao:
//                if (pop != null) {
//                    pop.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_textView_quxiaoo:
//                if (pops != null) {
//                    pops.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_textView_quxiaooo:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//
//                break;
//            case R.id.third_popupwindow_textView_look:
//                if (pop != null) {
//                    pop.dismiss();
//
//                }
//                intent = new Intent(this, ShowImageActivity.class);
//                if (user.getIcon().equals("")) {
//                    path = user.getSex();
//                } else {
//                    path = user.getIcon();
//                }
//                intent.putExtra("path", path);
//                intent.putExtra("type", "icon");
//                startActivity(intent);
//
//                break;
//            case R.id.third_popupwindow_textView_change:
//
//                showPopupWindow(codes);
//
//
//                break;
//            case R.id.third_popupwindow_textView_status://设置状态
//
//                showPopupWindow(codess);
//
//
//                break;
//            case R.id.third_popupwindow_textView_photo:
//                if (pops != null) {
//                    pops.dismiss();
//
//                }
//                if (user != null) {
//                    mPhotoSelectedHelper.imageSelection(user.getUser(), "pic");
//                }
//
//                break;
//
//            case R.id.third_popupwindow_textView_camera:
//                if (pops != null) {
//                    pops.dismiss();
//
//                }
//                if (user != null) {
//                    mPhotoSelectedHelper.imageSelection(user.getUser(), "take");
//                }
//
//                break;
//            case R.id.third_popupwindow_textView_status_online:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(0);
//                break;
//            case R.id.third_popupwindow_textView_status_qme:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(1);
//                break;
//            case R.id.third_popupwindow_textView_status_busy:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(2);
//                break;
//            case R.id.third_popupwindow_textView_status_wurao:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(3);
//                break;
//            case R.id.third_popupwindow_textView_status_leave:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(4);
//                break;
//            case R.id.third_popupwindow_textView_status_yinshen:
//                if (popss != null) {
//                    popss.dismiss();
//
//                }
//                setPresence(5);
//                break;
        }
    }

    long newTime;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        } else {
            if (System.currentTimeMillis() - newTime > 2000) {
                newTime = System.currentTimeMillis();
                Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(MainActivity.this, XmppService.class));
                unregisterReceiver(receiver);
//                ZuzhiiXMPP.disConnectServer();
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    public void onDismiss() {
        WindowManager.LayoutParams lp = getWindow()
                .getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
    }
}
