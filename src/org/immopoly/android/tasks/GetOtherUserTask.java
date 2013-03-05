package org.immopoly.android.tasks;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.immopoly.android.helper.WebHelper;
import org.immopoly.android.model.ImmopolyException;
import org.immopoly.android.model.ImmopolySimpleUser;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Returns another SimpleUser 
 * @author tobi
 */
public class GetOtherUserTask extends AsyncTask<String,Void,ImmopolySimpleUser> {
	
	private final Context mContext;
	protected Exception mException=null;
	
	public GetOtherUserTask(Context context) {
		mContext = context;
	}

	@Override
	protected ImmopolySimpleUser doInBackground(String... params) {
		String otherUserName = params[0];
		ImmopolySimpleUser otherUser = null;
		JSONObject obj = null;
		try {
			otherUserName = URLEncoder.encode(otherUserName, "UTF-8");
			obj = WebHelper.getHttpObjectData(new URL(
					WebHelper.SERVER_URL_PREFIX + "/user/profile/" + otherUserName+".json"),
					false, mContext);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (obj == null || obj.has("org.immopoly.common.ImmopolyException")) 
			mException = new ImmopolyException(mContext, obj);
		else{
			otherUser = new ImmopolySimpleUser();
			otherUser.fromJSON(obj);
		}
		
		return otherUser;
	}
	
	@Override
	protected void onPostExecute(ImmopolySimpleUser result) {
		super.onPostExecute(result);
		if (null != mException)
			Toast.makeText(this.mContext, mException.getMessage(), Toast.LENGTH_LONG).show();
	}


}
