package org.immopoly.android.app;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.immopoly.android.R;
import org.immopoly.android.fragments.PortfolioListFragment;
import org.immopoly.android.fragments.PortfolioMapFragment;
import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;
import org.immopoly.android.provider.FlatsProvider;
import org.immopoly.android.widget.FlatSelectListener;

import android.database.Cursor;
import android.graphics.Path.FillType;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

public class PortfolioActivity extends FragmentActivity implements FlatSelectListener {

	private Button mapButton;
	private Button listButton;
	private PortfolioListFragment listFragment;
	private PortfolioMapFragment mapFragment;
	private Flats flats;
	private FrameLayout switchFrame;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.portfolio);
		
		flats = queryFlats();
		
		mapButton   = (Button) findViewById( R.id.pf_map_btn );
		listButton  = (Button) findViewById( R.id.pf_list_btn );
		switchFrame = (FrameLayout) findViewById( R.id.pf_switch_frame );

		FragmentManager fm = getSupportFragmentManager();
		mapFragment  = (PortfolioMapFragment)  fm.findFragmentById( R.id.pf_map_fragment );
		listFragment = (PortfolioListFragment) fm.findFragmentById( R.id.pf_list_fragment );
		
		mapFragment.setFlats( flats );
		listFragment.setFlats( flats );
		
		if ( mapButton != null ) {
			mapButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					showMap();
				}
			});
			listButton.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					showList();
				}
			});
		}
	}

	private void showList() {
		switchFrame.bringChildToFront( listFragment.getView() );
		mapButton.setEnabled( true );
		listButton.setEnabled( false );
		switchFrame.invalidate();
	}

	private void showMap() {
		switchFrame.bringChildToFront( mapFragment.getView() );
		mapButton.setEnabled( false );
		listButton.setEnabled( true );
		switchFrame.invalidate();
	}
	
    private Flats queryFlats() {
        Cursor cur = getContentResolver().query(FlatsProvider.CONTENT_URI, null, null, null, null);
        Flats flats = new Flats();
        if ( cur.moveToFirst() )
            do {
            	Flat flat         = new Flat();
            	flat.uid          = cur.getInt( cur.getColumnIndex( FlatsProvider.Flat.FLAT_ID ) );
            	flat.name         = cur.getString( cur.getColumnIndex( FlatsProvider.Flat.FLAT_NAME ) );
            	flat.description  = cur.getString( cur.getColumnIndex( FlatsProvider.Flat.FLAT_DESCRIPTION ) );
            	// TODO lat & lng are swapped in the DB !?
            	flat.lng          = cur.getDouble( cur.getColumnIndex( FlatsProvider.Flat.FLAT_LATITUDE ) );
            	flat.lat          = cur.getDouble( cur.getColumnIndex( FlatsProvider.Flat.FLAT_LONGITUDE ) );
            	flat.creationDate = cur.getInt( cur.getColumnIndex( FlatsProvider.Flat.FLAT_CREATIONDATE ) ) * 1000;
            	flat.owned        = true;
            	flats.add( flat );
            } while ( cur.moveToNext() );
        cur.close();
        return flats;
    }


	@Override
    public void flatSelected( Flat flat ) {
		if ( switchFrame == null ) { // dual pane mode
			Log.w("IMPO", "PortfolioActivity.flatSelected" );
			listFragment.flatSelected( flat );
			mapFragment.flatSelected( flat );
		}
	}

	@Override
	public void flatClusterSelected(Flats flats) {
		if ( switchFrame == null ) { // dual pane mode
			Log.w("IMPO", "PortfolioActivity.flatClusterSelected" );
			listFragment.flatClusterSelected( flats );
			mapFragment.flatClusterSelected( flats );
		}
	}
}
