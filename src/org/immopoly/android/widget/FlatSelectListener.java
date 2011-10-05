package org.immopoly.android.widget;  // Help me! Im in the wrong package!

import java.util.ArrayList;

import org.immopoly.android.model.Flat;
import org.immopoly.android.model.Flats;

public interface FlatSelectListener {
	public void flatSelected( Flat flat );
	public void flatClusterSelected( Flats flats );
}
