/* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.display.screen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.content.ContentResolver;
import android.view.Display;
import android.util.ArrayMap;
import android.util.Log;

import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.old.Action;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class RotationFragment extends LeanbackPreferenceFragment {
	private static final String TAG = "RotationFragment";

	private static final String ROTATION_RADIO_GROUP = "rotation";
	private static final String ACTION_ROTATION_0 = "0";
	private static final String ACTION_ROTATION_90 = "90";
	private static final String ACTION_ROTATION_180 = "180";
	private static final String ACTION_ROTATION_270 = "270";


	private Context mContext;
	private static int degree;


	public static RotationFragment newInstance() {
		return new RotationFragment();
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		degree = display.getRotation();

		mContext = getPreferenceManager().getContext();
		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(mContext);
		screen.setTitle(R.string.screen_rotation);
		Preference activePref = null;

		final List<Action> InfoList = getActions();
		for (final Action info : InfoList) {
			final String tag = info.getKey();
			final RadioPreference radioPreference = new RadioPreference(mContext);
			radioPreference.setKey(tag);
			radioPreference.setPersistent(false);
			radioPreference.setTitle(info.getTitle());
			radioPreference.setRadioGroup(ROTATION_RADIO_GROUP);
			radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

			if (info.isChecked()) {
				radioPreference.setChecked(true);
				activePref = radioPreference;
			}

			screen.addPreference(radioPreference);
		}
		if (activePref != null && savedInstanceState == null) {
			scrollToPreference(activePref);
		}

		setPreferenceScreen(screen);
	}

	private ArrayList<Action> getActions() {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new Action.Builder().key(ACTION_ROTATION_0).title(getString(R.string.screen_rotation_0))
				.checked(degree == 0).build());
		actions.add(new Action.Builder().key(ACTION_ROTATION_90).title(getString(R.string.screen_rotation_90))
				.checked(degree == 1).build());
		actions.add(new Action.Builder().key(ACTION_ROTATION_180).title(getString(R.string.screen_rotation_180))
				.checked(degree == 2).build());
		actions.add(new Action.Builder().key(ACTION_ROTATION_270).title(getString(R.string.screen_rotation_270))
				.checked(degree == 3).build());
		return actions;
	}

	private void setRotation(int val) {
		android.provider.Settings.System.putInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
		android.provider.Settings.System.putInt(mContext.getContentResolver(), Settings.System.USER_ROTATION, val);
		SystemProperties.set("persist.sys.rotation", String.valueOf(val));
		degree = val;
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if (preference instanceof RadioPreference) {
			final RadioPreference radioPreference = (RadioPreference) preference;
			radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
			if (radioPreference.isChecked()) {
				String key = radioPreference.getKey().toString();
				if (key.equals(ACTION_ROTATION_0)) {
					setRotation(0);
				}
				if (key.equals(ACTION_ROTATION_90)) {
					setRotation(1);
				}
				if (key.equals(ACTION_ROTATION_180)) {
					setRotation(2);
				}
				if (key.equals(ACTION_ROTATION_270)) {
					setRotation(3);
				}
			} else {
				radioPreference.setChecked(true);
			}
		}
		return super.onPreferenceTreeClick(preference);
	}
}
