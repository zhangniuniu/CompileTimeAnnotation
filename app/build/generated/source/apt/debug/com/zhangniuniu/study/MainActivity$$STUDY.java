package com.zhangniuniu.study;

import android.widget.TextView;
import com.zhangniuniu.compiler.IViewInject;
import java.lang.Object;
import java.lang.Override;

public final class MainActivity$$STUDY implements IViewInject {
  @Override
  public void inject(Object target) {
    MainActivity substitute = (MainActivity)target;
    substitute.tvInfo = (TextView)substitute.findViewById(2131165321);
  }
}
