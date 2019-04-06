package qmars.dotir.usbsmartremote;

import android.content.Context;

public class RemoteClass {

	// g1
	private static final String POWER_BUTTON = "powerdb";
	private static final String VOLUP_BUTTON = "volupdb";
	private static final String VOLDOWN_BUTTON = "voldowndb";
	private static final String CHUP_BUTTON = "chupdb";
	private static final String CHDOWN_BUTTON = "chdowndb";
	private static final String MUTE_BUTTON = "mutedowndb";
	private static final String SRC_BUTTON = "srcdb";
	private static final String CHLIST_BUTTON = "chlistdb";

	private static final String NUM0_BUTTON = "num0db";
	private static final String NUM1_BUTTON = "num1db";
	private static final String NUM2_BUTTON = "num2db";
	private static final String NUM3_BUTTON = "num3db";
	private static final String NUM4_BUTTON = "num4db";
	private static final String NUM5_BUTTON = "num5db";
	private static final String NUM6_BUTTON = "num6db";
	private static final String NUM7_BUTTON = "num7db";
	private static final String NUM8_BUTTON = "num8db";
	private static final String NUM9_BUTTON = "num9db";
	private static final String NUMDASH_BUTTON = "numdashdb";
	private static final String NUMRET_BUTTON = "numretdb";

	// g2
	private static final String G2_RED_BUTTON = "g2reddb";
	private static final String G2_YELLOW_BUTTON = "g2yellowdb";
	private static final String G2_BLUE_BUTTON = "g2bluedb";
	private static final String G2_GREEN_BUTTON = "g2greendb";
	private static final String G2_VOLUP_BUTTON = "g2volupdb";
	private static final String G2_VOLDOWN_BUTTON = "voldowndb";
	private static final String G2_CHUP_BUTTON = "g2chupdb";
	private static final String G2_CHDOWN_BUTTON = "g2chdowndb";
	private static final String G2_INFO_BUTTON = "g2infdb";
	private static final String G2_HOME_BUTTON = "g2homedb";
	private static final String G2_GUIDE_BUTTON = "g2guidedb";
	private static final String G2_OK_BUTTON = "g2okdb";
	private static final String G2_G1_BUTTON = "g2g1db";
	private static final String G2_G2_BUTTON = "g2g2db";
	private static final String G2_G3_BUTTON = "g2g3db";
	private static final String G2_G4_BUTTON = "g2g4db";

	String powerCode;
	String volUpCode;
	String volDownCode;
	String chUpCode;
	String chDownCode;
	String muteCode;
	String srcCode;
	String chListCode;

	String num0Code;
	String num1Code;
	String num2Code;
	String num3Code;
	String num4Code;
	String num5Code;
	String num6Code;
	String num7Code;
	String num8Code;
	String num9Code;
	String numdashCode;
	String numretCode;

	// g2
	String g2redCode;
	String g2blueCode;
	String g2yellowCode;
	String g2greenCode;
	String g2okCode;
	String g2volupCode;
	String g2voldownCode;
	String g2chupCode;
	String g2chdownCode;
	String g2infoCode;
	String g2homeCode;
	String g2guideCode;
	String g2g1Code;
	String g2g2Code;
	String g2g3Code;
	String g2g4Code;

	private DatabaseHandler db;

	public RemoteClass(Context context, String dbName) {
		db = new DatabaseHandler(context, dbName, 1);

	}

	public void fetchCodes() {

		// g1
		powerCode = db.fetchRemote(POWER_BUTTON);
		volUpCode = db.fetchRemote(VOLUP_BUTTON);
		volDownCode = db.fetchRemote(VOLDOWN_BUTTON);
		chUpCode = db.fetchRemote(CHUP_BUTTON);
		chDownCode = db.fetchRemote(CHDOWN_BUTTON);
		muteCode = db.fetchRemote(MUTE_BUTTON);
		srcCode = db.fetchRemote(SRC_BUTTON);
		chListCode = db.fetchRemote(CHLIST_BUTTON);

		num0Code = db.fetchRemote(NUM0_BUTTON);
		num1Code = db.fetchRemote(NUM1_BUTTON);
		num2Code = db.fetchRemote(NUM2_BUTTON);
		num3Code = db.fetchRemote(NUM3_BUTTON);
		num4Code = db.fetchRemote(NUM4_BUTTON);
		num5Code = db.fetchRemote(NUM5_BUTTON);
		num6Code = db.fetchRemote(NUM6_BUTTON);
		num7Code = db.fetchRemote(NUM7_BUTTON);
		num8Code = db.fetchRemote(NUM8_BUTTON);
		num9Code = db.fetchRemote(NUM9_BUTTON);
		numdashCode = db.fetchRemote(NUMDASH_BUTTON);
		numretCode = db.fetchRemote(NUMRET_BUTTON);

		// g2
		g2redCode = db.fetchRemote(G2_RED_BUTTON);
		g2blueCode = db.fetchRemote(G2_BLUE_BUTTON);
		g2yellowCode = db.fetchRemote(G2_YELLOW_BUTTON);
		g2greenCode = db.fetchRemote(G2_GREEN_BUTTON);
		g2okCode = db.fetchRemote(G2_OK_BUTTON);
		g2volupCode = db.fetchRemote(G2_VOLUP_BUTTON);
		g2voldownCode = db.fetchRemote(G2_VOLDOWN_BUTTON);
		g2chupCode = db.fetchRemote(G2_CHUP_BUTTON);
		g2chdownCode = db.fetchRemote(G2_CHDOWN_BUTTON);
		g2infoCode = db.fetchRemote(G2_INFO_BUTTON);
		g2homeCode = db.fetchRemote(G2_HOME_BUTTON);
		g2guideCode = db.fetchRemote(G2_GUIDE_BUTTON);
		g2g1Code = db.fetchRemote(G2_G1_BUTTON);
		g2g2Code = db.fetchRemote(G2_G2_BUTTON);
		g2g3Code = db.fetchRemote(G2_G3_BUTTON);
		g2g4Code = db.fetchRemote(G2_G4_BUTTON);

	}

	public String getMuteCode() {
		return muteCode;
	}

	public String getSrcCode() {
		return srcCode;
	}

	public String getChListCode() {
		return chListCode;
	}

	public String getPowerCode() {
		return powerCode;
	}

	public String getVolUpCode() {
		return volUpCode;
	}

	public String getVolDownCode() {
		return volDownCode;
	}

	public String getChUpCode() {
		return chUpCode;
	}

	public String getChDownCode() {
		return chDownCode;
	}

	public void setPowerCode(String powerCode) {
		this.powerCode = powerCode;
		db.addDevice(POWER_BUTTON, powerCode);
	}

	public void setVolUpCode(String volUpCode) {
		this.volUpCode = volUpCode;
		db.addDevice(VOLUP_BUTTON, volUpCode);
	}

	public void setVolDownCode(String volDownCode) {
		this.volDownCode = volDownCode;
		db.addDevice(VOLDOWN_BUTTON, volDownCode);
	}

	public void setChUpCode(String chUpCode) {
		this.chUpCode = chUpCode;
		db.addDevice(CHUP_BUTTON, chUpCode);
	}

	public void setChDownCode(String chDownCode) {
		this.chDownCode = chDownCode;
		db.addDevice(CHDOWN_BUTTON, chDownCode);
	}

	public void setMuteCode(String muteCode) {
		this.muteCode = muteCode;
		db.addDevice(MUTE_BUTTON, muteCode);

	}

	public void setSrcCode(String srcCode) {
		this.srcCode = srcCode;
		db.addDevice(SRC_BUTTON, srcCode);
	}

	public void setChListCode(String chListCode) {
		this.chListCode = chListCode;
		db.addDevice(CHLIST_BUTTON, chListCode);
	}

	public String getG2redCode() {
		return g2redCode;
	}

	public String getG2blueCode() {
		return g2blueCode;
	}

	public String getG2yellowCode() {
		return g2yellowCode;
	}

	public String getG2greenCode() {
		return g2greenCode;
	}

	public String getG2okCode() {
		return g2okCode;
	}

	public String getG2volupCode() {
		return g2volupCode;
	}

	public String getG2voldownCode() {
		return g2voldownCode;
	}

	public String getG2chupCode() {
		return g2chupCode;
	}

	public String getG2chdownCode() {
		return g2chdownCode;
	}

	public String getG2g1Code() {
		return g2g1Code;
	}

	public String getG2g2Code() {
		return g2g2Code;
	}

	public String getG2g3Code() {
		return g2g3Code;
	}

	public String getG2g4Code() {
		return g2g4Code;
	}

	public void setG2redCode(String g2redCode) {
		this.g2redCode = g2redCode;
		db.addDevice(G2_RED_BUTTON, g2redCode);
	}

	public void setG2blueCode(String g2blueCode) {
		this.g2blueCode = g2blueCode;
		db.addDevice(G2_BLUE_BUTTON, g2blueCode);
	}

	public void setG2yellowCode(String g2yellowCode) {
		this.g2yellowCode = g2yellowCode;
		db.addDevice(G2_YELLOW_BUTTON, g2yellowCode);
	}

	public void setG2greenCode(String g2greenCode) {
		this.g2greenCode = g2greenCode;
		db.addDevice(G2_GREEN_BUTTON, g2greenCode);
	}

	public void setG2okCode(String g2okCode) {
		this.g2okCode = g2okCode;
		db.addDevice(G2_OK_BUTTON, g2okCode);
	}

	public void setG2volupCode(String g2volupCode) {
		this.g2volupCode = g2volupCode;
		db.addDevice(G2_VOLUP_BUTTON, g2volupCode);
	}

	public void setG2voldownCode(String g2voldownCode) {
		this.g2voldownCode = g2voldownCode;
		db.addDevice(G2_VOLDOWN_BUTTON, g2voldownCode);
	}

	public void setG2chupCode(String g2chupCode) {
		this.g2chupCode = g2chupCode;
		db.addDevice(G2_CHUP_BUTTON, g2chupCode);
	}

	public void setG2chdownCode(String g2chdownCode) {
		this.g2chdownCode = g2chdownCode;
		db.addDevice(G2_CHDOWN_BUTTON, g2chdownCode);
	}

	public void setG2g1Code(String g2g1Code) {
		this.g2g1Code = g2g1Code;
		db.addDevice(G2_G1_BUTTON, g2g1Code);
	}

	public void setG2g2Code(String g2g2Code) {
		this.g2g2Code = g2g2Code;
		db.addDevice(G2_G2_BUTTON, g2g2Code);
	}

	public void setG2g3Code(String g2g3Code) {
		this.g2g3Code = g2g3Code;
		db.addDevice(G2_G3_BUTTON, g2g3Code);
	}

	public void setG2g4Code(String g2g4Code) {
		this.g2g4Code = g2g4Code;
		db.addDevice(G2_G4_BUTTON, g2g4Code);
	}

	public String getNum0Code() {
		return num0Code;
	}

	public String getNum1Code() {
		return num1Code;
	}

	public String getNum2Code() {
		return num2Code;
	}

	public String getNum3Code() {
		return num3Code;
	}

	public String getNum4Code() {
		return num4Code;
	}

	public String getNum5Code() {
		return num5Code;
	}

	public String getNum6Code() {
		return num6Code;
	}

	public String getNum7Code() {
		return num7Code;
	}

	public String getNum8Code() {
		return num8Code;
	}

	public String getNum9Code() {
		return num9Code;
	}

	public String getNumdashCode() {
		return numdashCode;
	}

	public String getNumretCode() {
		return numretCode;
	}

	public void setNum0Code(String num0Code) {
		this.num0Code = num0Code;
		db.addDevice(NUM0_BUTTON, num0Code);
	}

	public void setNum1Code(String num1Code) {
		this.num1Code = num1Code;
		db.addDevice(NUM1_BUTTON, num1Code);
	}

	public void setNum2Code(String num2Code) {
		this.num2Code = num2Code;
		db.addDevice(NUM2_BUTTON, num2Code);
	}

	public void setNum3Code(String num3Code) {
		this.num3Code = num3Code;
		db.addDevice(NUM3_BUTTON, num3Code);
	}

	public void setNum4Code(String num4Code) {
		this.num4Code = num4Code;
		db.addDevice(NUM4_BUTTON, num4Code);
	}

	public void setNum5Code(String num5Code) {
		this.num5Code = num5Code;
		db.addDevice(NUM5_BUTTON, num5Code);
	}

	public void setNum6Code(String num6Code) {
		this.num6Code = num6Code;
		db.addDevice(NUM6_BUTTON, num6Code);
	}

	public void setNum7Code(String num7Code) {
		this.num7Code = num7Code;
		db.addDevice(NUM7_BUTTON, num7Code);
	}

	public void setNum8Code(String num8Code) {
		this.num8Code = num8Code;
		db.addDevice(NUM8_BUTTON, num8Code);
	}

	public void setNum9Code(String num9Code) {
		this.num9Code = num9Code;
		db.addDevice(NUM9_BUTTON, num9Code);
	}

	public void setNumdashCode(String numdashCode) {
		this.numdashCode = numdashCode;
		db.addDevice(NUMDASH_BUTTON, numdashCode);
	}

	public void setNumretCode(String numretCode) {
		this.numretCode = numretCode;
		db.addDevice(NUMRET_BUTTON, numretCode);
	}

	public String getG2infoCode() {
		return g2infoCode;
	}

	public String getG2homeCode() {
		return g2homeCode;
	}

	public String getG2guideCode() {
		return g2guideCode;
	}

	public void setG2infoCode(String g2infoCode) {
		this.g2infoCode = g2infoCode;
		db.addDevice(G2_INFO_BUTTON, g2infoCode);
	}

	public void setG2homeCode(String g2homeCode) {
		this.g2homeCode = g2homeCode;
		db.addDevice(G2_HOME_BUTTON, g2homeCode);
	}

	public void setG2guideCode(String g2guideCode) {
		this.g2guideCode = g2guideCode;
		db.addDevice(G2_GUIDE_BUTTON, g2guideCode);
	}
	
	
	
	

}
