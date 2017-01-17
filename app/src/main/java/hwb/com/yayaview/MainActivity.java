package hwb.com.yayaview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.LinkedList;

import hwb.com.yayaview.yayaview.SlidingVeiwX;
import yaya.slidingview.SlidingViewY;

public class MainActivity extends AppCompatActivity
{
    private SlidingVeiwX tvX;
    private SlidingViewY scrollView;
    private LinkedList<String> dataList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollView = (SlidingViewY) findViewById(R.id.scrollView);

        /*tvX = (SlidingVeiwX) findViewById(R.id.myTv);
        String str = "";
        String str1 = "";
        for(int i = 0; i < 100; i++)
        {
            str += ("测试数据" + i);
        }
        for(int i = 23; i >= 00; i--)
        {
//            str1 += (i + "\n");
            dataList.add(i + " : 00");
        }
        tvX.setText(str);*/
        scrollView.setSelectedPosition(2);
    }

}
