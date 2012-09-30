package org.immopoly.android.tasks;

import java.net.MalformedURLException;
import java.net.URL;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolySimpleUser;
import org.immopoly.common.SimpleUser;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

public class GetOtherUserTask extends AsyncTask<String,Void,SimpleUser> {
	
	private final Context mContext;
	
	public GetOtherUserTask(Context context) {
		mContext = context;
	}

	@Override
	protected SimpleUser doInBackground(String... params) {
		String otherUserName = params[0];
		ImmopolySimpleUser otherUser = null;
		JSONObject obj = null;
		
		try {
			obj = WebHelper.getHttpData(new URL(
					WebHelper.SERVER_URL_PREFIX + "/profile/" + otherUserName+".json"),
					false, mContext);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		otherUser = new ImmopolySimpleUser();
		otherUser.fromJSON(obj);
		
		return otherUser;
	}


}
