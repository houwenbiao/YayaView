# YayaView自定义控件主要实现惯性滑动功能
# 使用方法：
添加依赖：
compile 'com.github.houwenbiao:YayaView:1.0';
allprojects 
{
    repositories 
    {
        ......
        maven { url 'https://jitpack.io' }
    }
}
#layout中设置如下属性：
        android:layout_centerInParent="true"
        android:layout_width="150dp"
        android:layout_height="150dp"
        app:min_textSize="20sp"
        app:max_textSize="25sp"
        app:lineWidth="1dp"
#接口回调：
slidingViewY.setOnSelectedListener(new SlidingViewY.OnSelectedListener()
        {
            @Override
            public void onSelected(List<String> list, int i)
            {
                tv.setText(list.get(i));
            }
        });
