package ejm2.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class UIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "ejm2.ui";
    private static UIPlugin plugin;

    public UIPlugin() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        System.out.println("MyPlugin started");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        System.out.println("MyPlugin stopped");
    }

    public static UIPlugin getDefault() {
        return plugin;
    }
}