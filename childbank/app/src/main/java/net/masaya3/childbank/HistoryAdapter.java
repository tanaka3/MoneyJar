package net.masaya3.childbank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.masaya3.childbank.data.MoneyHistoryInfo;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class HistoryAdapter extends ArrayAdapter<MoneyHistoryInfo> {

    private static class ViewHolder {
        public TextView unitView;
        public TextView typeView;
        public TextView dayView;
        public TextView detailView;
        public TextView valueView;
    }

    private int mResource;

    private LayoutInflater mInflater;

    public HistoryAdapter(Context context, int resource, List<MoneyHistoryInfo> objects) {
        super(context, resource, objects);

        this.mResource = resource;

        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = this.mInflater.inflate(this.mResource, null);
            holder = new ViewHolder();

            holder.typeView = (TextView)convertView.findViewById(R.id.typeView);
            holder.detailView = (TextView)convertView.findViewById(R.id.detailView);
            holder.dayView = (TextView)convertView.findViewById(R.id.dayView);
            holder.valueView = (TextView)convertView.findViewById(R.id.valueView);
            holder.unitView = (TextView)convertView.findViewById(R.id.unitView);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        MoneyHistoryInfo history= ((MoneyHistoryInfo) getItem(position));



        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        holder.dayView.setText(df.format(history.time));


        String targetName = "(unknown)";
        if(history.targetInfo != null){
            targetName = history.targetInfo.name;
        }

        int typeId = 0;
        int colorId = 0;
        String detai_string = "";
        switch (history.type) {
            case OUT:
                typeId = R.string.hisotryOut;
                colorId = R.color.colorOut;
                detai_string = getContext().getString(R.string.detailOut);
                break;

            case SEND:
                typeId = R.string.hisotrySend;
                colorId = R.color.colorSend;
                detai_string = String.format(getContext().getString(R.string.detailSend), targetName);
                break;

            case NFC:
                typeId = R.string.hisotryNFC;
                colorId = R.color.colorNFC;
                detai_string = getContext().getString(R.string.detailNFC);
                break;
            default:
                typeId = R.string.hisotryIn;
                colorId = R.color.colorIn;
                detai_string = String.format(getContext().getString(R.string.detailIn), targetName);

                break;
        }

        holder.typeView.setText(typeId);
        holder.typeView.setBackgroundColor(getContext().getColor(colorId));

        NumberFormat nfNum = NumberFormat.getNumberInstance();
        holder.valueView.setText(nfNum.format(history.money));

        holder.detailView.setText(detai_string);

        holder.unitView.setText(ShareConfig.getInstance().moneyInfo.unit);

        return convertView;
    }
}
