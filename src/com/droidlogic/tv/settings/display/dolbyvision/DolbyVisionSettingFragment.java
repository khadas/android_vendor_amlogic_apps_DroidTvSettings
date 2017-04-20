/* Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.droidlogic.tv.settings.display.dolbyvision;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.ArrayMap;
import android.util.Log;
import android.text.TextUtils;

import com.droidlogic.app.DolbyVisionSettingManager;
import com.droidlogic.tv.settings.R;
import com.droidlogic.tv.settings.RadioPreference;
import com.droidlogic.tv.settings.dialog.old.Action;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class DolbyVisionSettingFragment extends LeanbackPreferenceFragment implements
        Preference.OnPreferenceChangeListener{
    private static final String TAG = "DolbyVisionSettingFragment";

    public static final String KEY_DOLBY_VISION     = "dolby_vision_set";

    private static final String DV_ENABLE            = "Y";
    private static final String DV_DISABLE           = "N";

    public static final int DOLBY_VISION_FOLLOW_SINK       = 0;
    public static final int DOLBY_VISION_FOLLOW_SOURCE     = 1;
    public static final int DOLBY_VISION_FORCE_OUTPUT_MODE = 2;

    public static final int DOLBY_VISION_OUTPUT_MODE_BYPASS      = 0;
    public static final int DOLBY_VISION_OUTPUT_MODE_IPT_TUNNEL  = 2;
    public static final int DOLBY_VISION_OUTPUT_MODE_HDR10       = 3;
    public static final int DOLBY_VISION_OUTPUT_MODE_SDR8        = 5;

    public static final String DV_OFF                    = "dolby_vision_off";
    public static final String DV_FOLLOW_SINK            = "dolby_vision_sink";
    public static final String DV_FOLLOW_SOURCE          = "dolby_vision_source";

    private DolbyVisionSettingManager mDolbyVisionSettingManager;

    private ListPreference dvPref;

    public static DolbyVisionSettingFragment newInstance() {
        return new DolbyVisionSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dolby_vision, null);
        mDolbyVisionSettingManager = new DolbyVisionSettingManager((Context)getActivity());

        dvPref = (ListPreference) findPreference(KEY_DOLBY_VISION);
        dvPref.setOnPreferenceChangeListener(this);

        int policy = mDolbyVisionSettingManager.getDolbyVisionPolicy();
        switch (policy) {
            case DOLBY_VISION_FOLLOW_SINK:
                dvPref.setValue(DV_FOLLOW_SINK);
                break;
            case DOLBY_VISION_FOLLOW_SOURCE:
                dvPref.setValue(DV_FOLLOW_SOURCE);
                break;
            case DOLBY_VISION_FORCE_OUTPUT_MODE:
                dvPref.setValue(DV_OFF);
                break;
        }
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_DOLBY_VISION)) {
            final String selection = (String) newValue;
            switch (selection) {
                case DV_OFF:
                    mDolbyVisionSettingManager.setDolbyVisionPolicy(DOLBY_VISION_FORCE_OUTPUT_MODE);
                    mDolbyVisionSettingManager.setDolbyVisionMode(DOLBY_VISION_OUTPUT_MODE_BYPASS);
                    mDolbyVisionSettingManager.setDolbyVisionEnable(false);
                    break;
                case DV_FOLLOW_SINK:
                    mDolbyVisionSettingManager.setDolbyVisionPolicy(DOLBY_VISION_FOLLOW_SINK);
                    mDolbyVisionSettingManager.setDolbyVisionEnable(true);
                    mDolbyVisionSettingManager.setDolbyVisionMode(DOLBY_VISION_OUTPUT_MODE_IPT_TUNNEL);
                    break;
                case DV_FOLLOW_SOURCE:
                    mDolbyVisionSettingManager.setDolbyVisionPolicy(DOLBY_VISION_FOLLOW_SOURCE);
                    mDolbyVisionSettingManager.setDolbyVisionEnable(true);
                    mDolbyVisionSettingManager.setDolbyVisionMode(DOLBY_VISION_OUTPUT_MODE_IPT_TUNNEL);
                    break;
                default:
                    //throw new IllegalArgumentException("Unknown dolby vision policy pref value");
                    break;
            }
            return true;
        }
        return true;
    }
}
