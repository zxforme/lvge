package lvge.com.myapp.modules.my_4s_management;

import android.graphics.Bitmap;

/**
 * Created by mac on 2017/7/8.
 */

public class SalesConsutantListViewData {

    private String name;
    private String phone;
    private String memo;
    private String headlmg;

    public String getName(){
        return  name;
    }

    public String getPhone(){
        return phone;
    }

    public String getMemo(){
        return memo;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getHeadlmg() {
        return headlmg;
    }

    public void setHeadlmg(String headlmg) {
        this.headlmg = headlmg;
    }
}
