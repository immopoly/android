package org.immopoly.android.model;

import org.immopoly.android.R;
import org.json.JSONObject;

import android.content.Context;

public class ImmopolyException extends org.immopoly.common.ImmopolyException {

	public ImmopolyException(Exception t) {
		super(t);
	}

	public ImmopolyException(Context context, JSONObject jsonObject) {
		super(jsonObject);
		switch (errorCode) {
		case 201:
			message = context.getString(R.string.flat_already_in_portifolio);
			break;
		case 301:
			message = context.getString(R.string.flat_does_not_exist_anymore);
			break;
		case 302:
			message = context.getString(R.string.flat_has_no_raw_rent);
			break;
		case 441:
			message = context.getString(R.string.expose_location_spoofing);
			break;
		default:
		}
	}
}
