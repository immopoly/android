/*
 * This is the Android component of Immopoly
 * http://immopoly.appspot.com
 * Copyright (C) 2011 Tobias Sasse
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

package org.immopoly.android.api;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class IS24ApiServiceMockup extends IntentService {

	private static final String SEARCH_VALUE = "search";
	private static final String RADIUS = "radius";
	public static final String COMMAND = "command";
	public static final String API_RECEIVER = "api_receiver";
	public static final String LAT = "lat";
	public static final String LNG = "lng";

	private static final String RESULTLIST_PREFIX = "/sdcard/immoploy_is24_resultlist_";
	
	public IS24ApiServiceMockup() {
		super("");
		Log.w( "IS24MOCK", "WARNING: IS24ApiServiceMockup is running" );
		// TODO Auto-generated constructor stub
	}
	
	public static final int CMD_REGION = 0x10;
	public static final int CMD_SEARCH = 0x11;
	
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_ERROR = 3;

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra(API_RECEIVER);
		final int cmd = intent.getIntExtra(COMMAND,-1);
		Bundle b = new Bundle();

		try {
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
			if ( cmd != CMD_SEARCH ) {
				b.putString(Intent.EXTRA_TEXT, "IS24ApiServiceMockup - Command != 'CMD_SEARCH' !!!" );
				receiver.send(STATUS_ERROR, b);
				return;
			}
				
			Flats flats = new Flats();

//			lat = intent.getDoubleExtra(LAT, 0.0);
//			lng = intent.getDoubleExtra(LNG, 0.0);
//			r = intent.getFloatExtra(RADIUS, 3.0f);
//			08-27 23:07:45.620: INFO/IMPO(22629): Mockup!!!!!! ! After SEARCH obj: {"resultlist.resultlist":{"resultlistEntries":[{"distance":0.17,"realEstateId":61751283,"@id":"61751283","@xlink.href":"http:\/\/rest.immobilienscout24.de\/restapi\/api\/search\/v1.0\/expose\/61751283","resultlist.realEstate":{"energyPerformanceCertificate":"true","courtage":{"hasCourtage":"YES"},"@xsi.type":"search:ApartmentRent","numberOfRooms":3,"livingSpace":92,"builtInKitchen":"true","title":"Topsanierte 3-Altbau-Zimmer-Wohnung mit 2 Balkonen gesucht?!","price":{"marketingType":"RENT","value":644,"priceIntervalType":"MONTH","currency":"EUR"},"titlePicture":{"floorplan":"false","@id":"","@xlink.href":"http:\/\/picture.immobilienscout24.de\/files\/result002\/N\/100\/317\/478\/100317478-4.jpg?951136146","urls":[{"url":{"@href":"http:\/\/picture.immobilienscout24.de\/files\/odw002\/N\/100\/317\/478\/100317478-3.jpg?951136146","@scale":"SCALE_118x118"}}],"@modification":"2011-08-25T04:13:33.247+02:00","@creation":"2011-08-25T04:13:33.247+02:00"},"address":{"houseNumber":"18","wgs84Coordinate":{"longitude":13.453558784675147,"latitude":52.52289072725408},"postcode":"10249","quarter":"Friedrichshain (Friedrichshain)","city":"Berlin","street":"Ebertystraße"},"floorplan":"false","@id":"61751283","garden":"false","certificateOfEligibilityNeeded":"false","balcony":"true"},"@modification":"2011-08-23T13:18:57.000+02:00","@creation":"2011-08-17T11:24:58.000+02:00"},{"distance":0.2,"realEstateId":61724698,"@id":"61724698","@xlink.href":"http:\/\/rest.immobilienscout24.de\/restapi\/api\/search\/v1.0\/expose\/61724698","resultlist.realEstate":{"energyPerformanceCertificate":"true","courtage":{"hasCourtage":"NO"},"@xsi.type":"search:ApartmentRent","numberOfRooms":1,"livingSpace":50,"builtInKitchen":"true","title":"**Top Single-Altbauwohnung mitten im Kiez, ruhig, zentral, provisionsfrei!**","price":{"marketingType":"RENT","value":425,"priceIntervalType":"MONTH","currency":"EUR"},"titlePicture":{"floorplan":"true","@id":"","@xlink.href":"http:\/\/picture.immobilienscout24.de\/files\/result001\/N\/100\/2\/396\/100002396-4.jpg?3410438580","urls":[{"url":{"@href":"http:\/\/picture.immobilienscout24.de\/files\/odw001\/N\/100\/2\/396\/100002396-3.jpg?3410438580","@scale":"SCALE_118x118"}}],"@modification":"2011-08-25T04:13:33.249+02:00","@creation":"2011-08-25T04:13:33.249+02:00"},"address":{"houseNumber":"20","wgs84Coordinate":{"longitude":13.453882509965952,"latitude":52.52242511734749},"postcode":"10249","quarter":"Friedrichshain (Friedrichshain)","city":"Berlin","street":"Ebertystraße"},"floorplan":"true","@id":"61724698","garden":"false","certificateOfEligibilityNeeded":"false","balcony":"false"},"@modification":"2011-08-24T10:41:35.000+02:00","@creation":"2011-08-15T11:12:41.000+02:00"}],"paging":{"pageSize":20,"numberOfHits":984,"pageNumber":1,"numberOfPages":50,"next":{"@xlink.href":"http:\/\/rest.immobilienscout24.de\/restapi\/api\/search\/v1.0\/search\/radius?realEstateType=apartmentrent&pagenumber=2&geocoordinates=52.52295838571428;13.451150557142856;3.0"}}}}

			final int size = 4;
			for (int i = 1; i < size; i++) {
				File f = new File( RESULTLIST_PREFIX + i );
				if ( ! f.exists() )
					break;
				char[] buf = new char[ (int) f.length() ];
				int n = new FileReader( f ).read( buf );
				JSONTokener tokener = new JSONTokener( new String( buf, 0, n ) );
				JSONObject obj = new JSONObject(tokener);
				flats.parse(obj);
			}
			b.putParcelableArrayList("flats", flats);
			receiver.send(STATUS_FINISHED, b);
		} catch (Exception e) {
			Log.e( "IS24MOCK", "Exception in IS24ApiServiceMockup: ", e );
			b.putString(Intent.EXTRA_TEXT, e.toString());
			receiver.send(STATUS_ERROR, b);
		}
		this.stopSelf();

	}

}
