package me.avery246813579.universalcredits;

public class Transfer {
	private String sender;
    private String receiver;
    private int amount;
    private String type;
    private boolean status;
    private String error;
    public static String PLAYER_PAYMENT = "player payment";
    public static String SIGN_PURCHASE = "sign purchase";
    public static String MONEY_RESET = "money reset";
    public static String MONEY_GIVE = "money give";
    public static String MONEY_SET = "money set";
    public static String SILENT = null;

    public Transfer(String send, String receive, int money, String msg) {
        this.sender = send;
        this.receiver = receive;
        this.amount = money;
        this.type = msg;
    }

    public void setError(String er) {
        this.error = er;
    }

    public String getError() {
        return this.error;
    }

    public void setStatus(boolean stat) {
        this.status = stat;
    }

    public boolean getStatus() {
        return this.status;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getType() {
        return this.type;
    }

	public static String getPLAYER_PAYMENT() {
		return PLAYER_PAYMENT;
	}

	public static void setPLAYER_PAYMENT(String pLAYER_PAYMENT) {
		PLAYER_PAYMENT = pLAYER_PAYMENT;
	}

	public static String getSIGN_PURCHASE() {
		return SIGN_PURCHASE;
	}

	public static void setSIGN_PURCHASE(String sIGN_PURCHASE) {
		SIGN_PURCHASE = sIGN_PURCHASE;
	}

	public static String getMONEY_RESET() {
		return MONEY_RESET;
	}

	public static void setMONEY_RESET(String mONEY_RESET) {
		MONEY_RESET = mONEY_RESET;
	}

	public static String getMONEY_GIVE() {
		return MONEY_GIVE;
	}

	public static void setMONEY_GIVE(String mONEY_GIVE) {
		MONEY_GIVE = mONEY_GIVE;
	}

	public static String getMONEY_SET() {
		return MONEY_SET;
	}

	public static void setMONEY_SET(String mONEY_SET) {
		MONEY_SET = mONEY_SET;
	}

	public static String getSILENT() {
		return SILENT;
	}

	public static void setSILENT(String sILENT) {
		SILENT = sILENT;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setType(String type) {
		this.type = type;
	}
}
