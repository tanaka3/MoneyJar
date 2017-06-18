package net.masaya3.childbank;

import net.masaya3.childbank.data.MoneyInfo;
import net.masaya3.childbank.data.UserInfo;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class ShareConfig {

    private static ShareConfig mConfig;

    public static ShareConfig getInstance(){
        if(mConfig == null){
            mConfig = new ShareConfig();
        }

        return mConfig;
    }

    private ShareConfig(){
        //デモ
        moneyInfo =  new MoneyInfo();
        moneyInfo.unit ="田中円";

        userInfo = new UserInfo();
    }

    public MoneyInfo moneyInfo;
    public UserInfo userInfo;
}
