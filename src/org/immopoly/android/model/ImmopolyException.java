package org.immopoly.android.model;

import org.immopoly.android.R;
import org.immopoly.android.constants.Const;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ImmopolyException extends org.immopoly.common.ImmopolyException {

	public ImmopolyException(Exception t) {
		super(t);
	}

	public ImmopolyException(String msg,Exception t) {
		super(t);
		message=msg;
	}
	
	public ImmopolyException(Context context, JSONObject jsonObject) {
		super(jsonObject);
		switch (errorCode) {
		case USERNAME_NOT_FOUND:
			message = context.getString(R.string.account_not_found);
			break;
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
		case USER_SEND_PASSWORDMAIL_NOEMAIL:
			message = context.getString(R.string.account_has_no_email);;
			break;
		case USER_SEND_PASSWORDMAIL_EMAIL_NOMATCH:
			message = context.getString(R.string.account_has_different_email);;
			break;
		default:
			Log.i( Const.LOG_TAG, "not yet translated ImmopolyException errorCode: " + errorCode );
		}
	}
}
