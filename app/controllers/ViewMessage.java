package controllers;

public enum ViewMessage {
	NO_MESSAGE(""),
	ERROR_ITEM_DELETE("この商品は使用されています。削除できません"),
	ERROR_TAX_INSERT("該当期間に別の税率が既に登録されています"),
	ERROR_NO_ORDER("商品が注文されていません"),
	ANY_ERROR_ALL("何かエラー発生っす"),
	NULLP_ERROR_ALL("NullPointer発生っす");
	
	private String message;
	private ViewMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
