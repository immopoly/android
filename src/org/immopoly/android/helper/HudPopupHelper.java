package org.immopoly.android.helper;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class HudPopupHelper {

	public static final int TYPE_FINANCE_POPUP = 0x1;
	private final static NumberFormat nFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

	private LinearLayout mLayoutView = null;
	private PopupWindow mPopupView = null;

	public HudPopupHelper(Activity activity, int type) {
		switch (type) {
		case TYPE_FINANCE_POPUP:
			mLayoutView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.hud_statistic, null);
			break;

		default:
			mLayoutView = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.hud_statistic, null);
			break;
		}
		if (mLayoutView != null) {
			mPopupView = new PopupWindow(mLayoutView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
			mPopupView.setOutsideTouchable(true);
			mPopupView.setBackgroundDrawable(new BitmapDrawable());
			mPopupView.setTouchable(true);
			mPopupView.setTouchInterceptor(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// do this later - otherwise HudFragment would pup me up again imidiately (better ideas welcome) 
					new Handler().postDelayed(new Runnable() {
						public void run() {
							mPopupView.dismiss();
						}
					}, 200 );
					return true;
				}
			});
		}
	}

	public void show(View view, int xOffset, int yOffset) {
		if (mPopupView != null) {
			if (!mPopupView.isShowing()) {
				ImmopolyUser user = ImmopolyUser.getInstance();

				TextView currentCostTextView = (TextView) mLayoutView.findViewById(R.id.current_cost);
				if (currentCostTextView != null) {
					nFormat.setMinimumIntegerDigits(1);
					nFormat.setMaximumFractionDigits(0);
					nFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

					currentCostTextView.setText(nFormat.format(user.getLastRent()));
				}
				
				String calculatedCosts = nFormat.format((int) (user.getLastRent() * 30));
				TextView calculatedCostTextView = (TextView) mLayoutView.findViewById(R.id.calculated_cost);
				if (calculatedCostTextView != null) {
					calculatedCostTextView.setText(calculatedCosts);
				}

				String lastProvision = nFormat.format((int) (user.getLastProvision()));
				TextView lastProvisionTextView = (TextView) mLayoutView.findViewById(R.id.last_provision);
				if (lastProvisionTextView != null) {
					lastProvisionTextView
							.setText(lastProvision);
				}
				
				TextView balanceCostTextView = (TextView) mLayoutView.findViewById(R.id.current_balance);
				if (balanceCostTextView != null) {
					balanceCostTextView.setText(nFormat.format(user.getBalance()));
				}
				
				TextView numFlatsView = (TextView) mLayoutView.findViewById( R.id.num_flats );
				numFlatsView.setText( user.getPortfolio().size() +" / 30" );

				mPopupView.showAsDropDown(view, view.getWidth(), 0 );
			} else {
				mPopupView.dismiss();
			}
		}

	}

	public boolean isShowing() {
		return (mPopupView != null && mPopupView.isShowing());
	}

	public void dismiss() {
		if (mPopupView != null) {
			mPopupView.dismiss();
		}
	}
}
