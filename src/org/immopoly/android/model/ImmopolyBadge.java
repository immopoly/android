package org.immopoly.android.model;

import org.immopoly.common.Badge;
import org.json.JSONException;
import org.json.JSONObject;

public class ImmopolyBadge extends Badge {

	// mandatory
	private int type;
	private String text;
	private String url;
	private long time;

	// optional
	private Double amount;
	private Long exposeId;

	public ImmopolyBadge(JSONObject o) {
		super(o);
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public void setExposeId(long exposeId) {
		this.exposeId = exposeId;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}

	public long getTime() {
		return time;
	}

	public Double getAmount() {
		return amount;
	}

	public Long getExposeId() {
		return exposeId;
	}

	/**
	 * Wird im Client nicht mehr serialisiert.
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		throw new UnsupportedOperationException();
	}
}
