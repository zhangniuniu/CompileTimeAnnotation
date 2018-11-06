package com.zhangniuniu.study;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zhangniuniu.compiler.BindView;
import com.zhangniuniu.compiler.InjectUtils;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_info)
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InjectUtils.inject(this);

        tvInfo.setText("我不是空哦，可以去build/generated/source/apt/debug下查看编译时生成的文件");
    }
}
