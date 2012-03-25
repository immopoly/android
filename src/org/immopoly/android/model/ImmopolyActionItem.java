package org.immopoly.android.model;

import org.immopoly.common.ActionItem;
import org.json.JSONException;
import org.json.JSONObject;

public class ImmopolyActionItem extends ActionItem {

	// mandatory
	private int type;
	private String text;
	private String url;
	private int amount;

	public ImmopolyActionItem(JSONObject o) {
		super(o);
	}

	
	/**
	 * Wird im Client nicht mehr serialisiert.
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}
}
