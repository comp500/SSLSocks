package link.infra.sslsocks.gui.main;

import android.app.Activity;
import android.os.Bundle;

import link.infra.sslsocks.service.StunnelIntentService;

public class ServiceShortcutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StunnelIntentService.start(this);
		finish();
	}
}
