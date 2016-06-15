package com.marktony.zhihudaily.ui.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.marktony.zhihudaily.R;
import com.marktony.zhihudaily.utils.Api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 2016.6.15 黎赵太郎
 * 果壳文章阅读
 */
public class GuokrReadActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ImageView ivHeadline;
    private WebView wbMain;
    private CollapsingToolbarLayout toolbarLayout;

    private MaterialDialog loadingDialog;

    private String id;
    private String headlineUrl;
    private String title;

    private SharedPreferences sp;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guokr_read);

        initViews();

        sp = getSharedPreferences("user_settings",MODE_PRIVATE);

        queue = Volley.newRequestQueue(getApplicationContext());

        loadingDialog = new MaterialDialog.Builder(GuokrReadActivity.this)
                .content(getString(R.string.loading))
                .progress(true,0)
                .build();

        loadingDialog.show();

        id = getIntent().getStringExtra("id");
        headlineUrl = getIntent().getStringExtra("headlineImageUrl");
        title = getIntent().getStringExtra("title");

        setCollapsingToolbarLayoutTitle(title);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
                String shareText = title + " " +  Api.GUOKR_ARTICLE_LINK_V1 + id + getString(R.string.share_extra);
                shareIntent.putExtra(Intent.EXTRA_TEXT,shareText);
                startActivity(Intent.createChooser(shareIntent,getString(R.string.share_to)));
            }
        });

        if (headlineUrl != null){
            Glide.with(GuokrReadActivity.this)
                    .load(headlineUrl)
                    .asBitmap()
                    .centerCrop()
                    .into(ivHeadline);
        } else {
            ivHeadline.setImageResource(R.drawable.no_img);
        }

        // 设置是否加载图片，true不加载，false加载图片sp.getBoolean("no_picture_mode",false)
        wbMain.getSettings().setBlockNetworkImage(true);

        //能够和js交互
        wbMain.getSettings().setJavaScriptEnabled(true);
        //缩放,设置为不能缩放可以防止页面上出现放大和缩小的图标
        wbMain.getSettings().setBuiltInZoomControls(false);
        //缓存
        wbMain.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //开启DOM storage API功能
        wbMain.getSettings().setDomStorageEnabled(true);
        //开启application Cache功能
        wbMain.getSettings().setAppCacheEnabled(false);
        //不调用第三方浏览器即可进行页面反应
        wbMain.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                wbMain.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                super.onPageFinished(view, url);
            }
        });

        // 设置在本WebView内可以通过按下返回上一个html页面
        wbMain.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK && wbMain.canGoBack()){
                        wbMain.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

        Log.d("url",Api.GUOKR_ARTICLE_BASE_URL + "?pick_id" + id);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Api.GUOKR_ARTICLE_BASE_URL + "?pick_id" + id, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("ok").equals("true")){
                        Log.d("jjjjjj",jsonObject.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("err",volleyError.toString());
            }
        });

        queue.add(request);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_guokr_read,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        if (item.getItemId() == R.id.action_open_in_browser){
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Api.GUOKR_ARTICLE_LINK_V1 + id)));
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivHeadline = (ImageView) findViewById(R.id.iv_guokr_headline);
        wbMain = (WebView) findViewById(R.id.wb_guokr_read);

    }

    // to change the title's font size of toolbar layout
    private void setCollapsingToolbarLayoutTitle(String title) {
        toolbarLayout.setTitle(title);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarPlus1);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBarPlus1);
    }

}
