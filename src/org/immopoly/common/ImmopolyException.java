package org.immopoly.common;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class ImmopolyException extends Exception implements JSONable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 107444085836140773L;

	public static final int MISSING_PARAMETER_USERNAME = 43;

	public static final int MISSING_PARAMETER_PASSWORD = 44;

	public static final int MISSING_PARAMETER_START_END = 93;

	public static final int PARAMETER_START_END_TO_WIDE = 94;
	
	public static final int REGISTER_USERNAME_ALREADY_TAKEN = 45;

	public static final int REGISTER_FAILED = 101;

	public static final int MISSING_PARAMETER_TOKEN = 61;

	public static final int TOKEN_NOT_FOUND = 62;

	public static final int USERNAME_NOT_FOUND = 63;
	
	public static final int EXPOSE_ALREADY_IN_PORTFOLIO = 201;

	public static final int EXPOSE_NOT_OWNED = 202;
	
	public static final int EXPOSE_NOT_FOUND = 301;

	public static final int EXPOSE_NO_RENT = 302;

	public static final int EXPOSE_ADD_FAILED = 101;

	public static final int EXPOSE_REMOVE_FAILED = 303;
	
	public static final int EXPOSE_NO_COURTAGE = 304;

	public static final int C2DM_ID_NOT_FOUND = 81;

	public static final int C2DM_FAILED = 83;

	public static final int USER_INFO_FAILED = 101;

	public static final int NO_MORE_DATA = 94;

	public static final int TOPX_FAILED = 95;

	public static final int MISSING_PARAMETER_C2DM_ID =69;

	public static final int C2DM_REGISTER_FAILED = 108;

	public static final int HISTORY_FAILED = 101;

	public static final int USER_LOGIN_FAILED = 101;

	public static final int USER_DOES_NOT_MATCH_TOKEN = 52;

	public static final int USER_PASSWORD_CHANGE_FAILED = 108;

	public static final int MISSING_PARAMETER_EMAIL = 45;

	public static final int USER_SEND_PASSWORDMAIL_FAILED = 109;

	public static final int EXPOSE_MAX_NUM = 305;

	public static final int EXPOSE_HEATMAP_FAILED = 1001;

	public static final int USER_SEND_PASSWORDMAIL_NOEMAIL = 450;
	public static final int USER_SEND_PASSWORDMAIL_EMAIL_NOMATCH = 451;

	public static final int ACTIONITEM_NOTFOUND = 2001;

	static Logger LOG = Logger.getLogger(ImmopolyException.class.getName());

	protected String name = null;
	protected String message = null;
	protected int errorCode = 1;
	protected Exception cause = null;


	public ImmopolyException()
	{
		super();
	}


	private ImmopolyException(String message)
	{
		super();
		this.message = message;
		this.name = this.getClass().getName();
	}


	public ImmopolyException(int errorCode, String message)
	{
		this(message);
		this.errorCode = errorCode;
	}

	public ImmopolyException(Exception t)
	{
		this(t.getMessage());
		this.name = t.getClass().getName();
		if (null != t.getCause())
			cause = new Exception(t.getCause());
	}


	public ImmopolyException(int errorCode, String message, Exception c)
	{
		this(errorCode, message);
		if (null != c)
			cause = c;
	}


	public ImmopolyException(JSONObject jsonObject)
	{
		fromJSON(jsonObject);
	}


	public JSONObject toJSON()
	{
		JSONObject o = new JSONObject();
		JSONObject properties = new JSONObject();
		try
		{
			properties.put("message",message);
			properties.put("errorCode",errorCode);
			if (null != cause)
			{
				if (cause instanceof ImmopolyException)
					properties.put("cause",((ImmopolyException) cause).toJSON());
				else
				{
					JSONObject co = new JSONObject();
					JSONObject cproperties = new JSONObject();
					cproperties.put("message",cause.getMessage());
					cproperties.put("errorCode",-1);
					co.put(cause.getClass().getName(), cproperties);
					properties.put("cause",co);
				}
			}
			o.put(name, properties);
		}
		catch (JSONException e)
		{
			try
			{
				return new JSONObject("{JSONException:[\"Konnte Exception nicht nach JSON schreiben '" + message + "'\"]}");
			}
			catch (JSONException e1)
			{
				LOG.log(Level.WARNING, "Konnte Exception nicht nach JSON schreiben '" + message + "'", e);
			}
		}
		return o;
	}


	@Override
	public Throwable getCause()
	{
		return cause;
	}


	@Override
	public String getMessage()
	{
		return this.message;
	}


	public int getErrorCode()
	{
		return errorCode;
	}


	@Override
	public void fromJSON(JSONObject jsonObject)
	{
		if(null==jsonObject)
			return;
		try
		{
			JSONObject properties = jsonObject.getJSONObject("org.immopoly.common.ImmopolyException");
			if(null!=properties)
			{
				this.message = properties.getString("message");
				this.errorCode = properties.getInt("errorCode");
			}
			// TODO schtief
//			if (properties.length() == 3)
//			{
//				this.cause = instantiateException(properties.getJSONObject(3));
//			}
		}
		catch (JSONException e)
		{
			if (null == this.message || message.length() == 0)
				this.message = "Could not parse JSONException " + e.getMessage();
			this.cause = e;
		}
	}


//	public static Exception instantiateException(JSONObject jsonObject)
//	{
//		String[] name = JSONObject.getNames(jsonObject);
//		if (!name[0].contains("Exception"))
//			return null;
//
//		// try to instantiate
//		Class exceptionClass;
//		Object exceptionO = null;
//		try
//		{
//			exceptionClass = Class.forName(name[0]);
//
//			// try find message constructor
//			if (null != jsonObject.optJSONArray(name[0]) && null != jsonObject.getJSONArray(name[0]).optString(0)
//					&& jsonObject.getJSONArray(name[0]).optString(0).length() > 0)
//			{
//				Constructor[] constructors = exceptionClass.getConstructors();
//				for (int i = 0; i < constructors.length; i++)
//					if (constructors[i].getParameterTypes().length == 1 && constructors[i].getParameterTypes()[0].equals(String.class))
//						exceptionO = (Exception) constructors[i].newInstance(jsonObject.getJSONArray(name[0]).optString(0));
//			}
//			if (exceptionO == null)
//				exceptionO = exceptionClass.newInstance();
//		}
//		catch (Exception e)
//		{
//			LOG.error("Could not instantiate Exception: " + name[0], e);
//			return new JSONException(jsonObject);
//		}
//
//		// test if Exception
//		if (!(exceptionO instanceof JSONException))
//			return (Exception) exceptionO;
//
//		JSONException exception = (JSONException) exceptionO;
//		// if jsonable
//		exception.fromJSON(jsonObject);
//		return exception;
//	}


	public String getName()
	{
		return name;
	}

	public Level getLogLevel() {
		if (errorCode == EXPOSE_ADD_FAILED || errorCode == EXPOSE_ADD_FAILED || errorCode == EXPOSE_HEATMAP_FAILED
				|| errorCode == EXPOSE_REMOVE_FAILED || errorCode == HISTORY_FAILED || errorCode == REGISTER_FAILED
				|| errorCode == TOPX_FAILED || errorCode == USER_INFO_FAILED
				|| errorCode == USER_SEND_PASSWORDMAIL_FAILED || errorCode == USER_PASSWORD_CHANGE_FAILED)
			return Level.SEVERE;
		return Level.WARNING;
	}
}
