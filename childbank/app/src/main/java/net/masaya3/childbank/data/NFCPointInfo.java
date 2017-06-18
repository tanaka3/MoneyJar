package net.masaya3.childbank.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class NFCPointInfo implements Parcelable {
    public long mPoint;
    public int mPointTime;

    public NFCPointInfo(long point){

        this.mPoint = point;
        this.mPointTime = 1000;
    }

    public static NFCPointInfo parse(JSONObject json){

        if(json == null){
            return null;
        }

        if(!json.has("point")){
            return null;
        }

        if(!json.has("time")){
            return null;
        }

        try {
            return new NFCPointInfo(json.getInt("point"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject toJSONObject(){
        JSONObject json = new JSONObject();

        try {
            json.put("point", mPoint   );
            json.put("time", mPointTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private NFCPointInfo(Parcel parcel) {
        mPoint = parcel.readLong();
        mPointTime = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mPoint);
        parcel.writeInt(mPointTime);
    }

    public static final Parcelable.Creator<NFCPointInfo> CREATOR = new Parcelable.Creator<NFCPointInfo>() {
        public NFCPointInfo createFromParcel(Parcel in) {
            return new NFCPointInfo(in);
        }

        public NFCPointInfo[] newArray(int size) {
            return new NFCPointInfo[size];
        }
    };

}
