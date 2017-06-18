package net.masaya3.childbank.data;

import java.util.Date;

/**
 * Created by masaya3 on 2017/06/17.
 */

public class MoneyHistoryInfo {

    public enum HistoryType{
        IN(1),
        OUT(2),
        SEND(0),
        NFC(3),
        BANK_NFC(4),
        BANK_YEN(5),
        BLANL_A(6),
        BLANL_B(7),
        BLANL_C(8),
        BLANL_D(9),
        BLANL_E(10);


        private final int id;

        private HistoryType(final int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static HistoryType valueOf(int id) {
            for (HistoryType num : values()) {
                if (num.getId() == id) {
                    return num;
                }
            }

            throw new IllegalArgumentException("no such enum object for the id: " + id);
        }
    };

    public Date time;
    public long money;
    public HistoryType type = HistoryType.IN;
    public MoneyInfo moneyInfo=null;
    public UserInfo targetInfo=null;
}
