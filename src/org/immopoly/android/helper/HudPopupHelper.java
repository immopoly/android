package org.immopoly.android.helper;

import java.text.NumberFormat;
import java.util.Locale;

import org.immopoly.android.R;
import org.immopoly.android.model.ImmopolyUser;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class HudPopupHelper {

	public static final int TYPE_FINANCE_POPUP = 0x1;

	private LinearLayout mLayoutView = null;
	private PopupWindow mPopupView = null;

	public HudPopupHelper(Activity activity, int type) {
		switch (type) {
		case TYPE_FINANCE_POPUP:
			mLayoutView = (LinearLayout) activity.getLayoutInflater().inflate(
					R.layout.hud_statistic, null);
			break;

		default:
			mLayoutView = (LinearLayout) activity.getLayoutInflater().inflate(
					R.layout.hud_statistic, null);
			break;
		}
		if (mLayoutView != null) {
			mPopupView = new PopupWindow(mLayoutView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
	}

	public void show(View view, int xOffset, int yOffset) {
		if (mPopupView != null) {
			if (!mPopupView.isShowing()) {
				ImmopolyUser user = ImmopolyUser.getInstance();
				TextView currentCostTextView = (TextView) mLayoutView
						.findViewById(R.id.current_cost);
				if (currentCostTextView != null) {
					NumberFormat nFormat = NumberFormat
							.getCurrencyInstance(Locale.GERMANY);
					nFormat.setMinimumIntegerDigits(1);
					nFormat.setMaximumFractionDigits(2);

					currentCostTextView.setText(mLayoutView.getResources()
							.getString(R.string.current_cost_text,
									nFormat.format(user.getLastRent())));
					String calculatedCosts = nFormat.format((int) (user
							.getLastRent() * 30));
					TextView calculatedCostTextView = (TextView) mLayoutView
							.findViewById(R.id.calculated_cost);
					if (calculatedCostTextView != null) {
						calculatedCostTextView.setText(mLayoutView
								.getResources().getString(
										R.string.calculated_cost_text,
										calculatedCosts));
					}
					TextView balanceCostTextView = (TextView) mLayoutView
							.findViewById(R.id.current_balance);
					if (balanceCostTextView != null) {
						balanceCostTextView.setText(balanceCostTextView
								.getResources().getString(
										R.string.current_ballance_text,
										nFormat.format(user.getBalance())));
					}
				}
				mPopupView.showAsDropDown(view/* , xOffset, xOffset */);
			} else {
				mPopupView.dismiss();
			}
		}

	}
	
	public boolean isShowing(){
		return (mPopupView != null && mPopupView.isShowing());
	}
	
	public void dismiss() {
		if (mPopupView != null) {
			mPopupView.dismiss();
		}
	}
}
